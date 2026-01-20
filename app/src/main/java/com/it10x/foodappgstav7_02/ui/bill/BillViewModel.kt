package com.it10x.foodappgstav7_02.ui.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_02.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_02.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_02.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    // ✅ Expose orderType safely for Compose UI
    val orderTypePublic: String
        get() = orderType

    init {
        Log.d("BILL_INIT", "Initialized | table=$tableId")
        observeBill()
    }

    // --------------------------------------------------------
    // Observe Bill (Live billing snapshot)
    // --------------------------------------------------------
    private fun observeBill() {
        viewModelScope.launch {
            kotItemDao.getItemsForTable(tableId).collectLatest { kotItems ->
                val doneItems = kotItems.filter { it.status == "DONE" }

                val billingItems = doneItems
                    .groupBy { it.productId }
                    .map { (_, group) ->
                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }
                        val subtotal = group.sumOf { it.basePrice * it.quantity }
                        val taxTotal = group.sumOf {
                            if (it.taxType == "exclusive")
                                it.basePrice * it.quantity * (it.taxRate / 100)
                            else 0.0
                        }
                        BillingItemUi(
                            id = first.productId,
                            name = first.name,
                            quantity = quantity,
                            subtotal = subtotal,
                            taxTotal = taxTotal,
                            finalTotal = subtotal + taxTotal
                        )
                    }

                val subtotal = billingItems.sumOf { it.subtotal }
                val tax = billingItems.sumOf { it.taxTotal }

                _uiState.value = BillUiState(
                    loading = false,
                    items = billingItems,
                    subtotal = subtotal,
                    tax = tax,
                    total = subtotal + tax
                )
            }
        }
    }

    // --------------------------------------------------------
    // Payment + Order Creation
    // --------------------------------------------------------
    fun payBill(paymentType: String) {
        viewModelScope.launch {
            val kotItems = kotItemDao.getItemsForTableSync(tableId).filter { it.status == "DONE" }
            if (kotItems.isEmpty()) return@launch

            val itemSubtotal = kotItems.sumOf { it.basePrice * it.quantity }
            val taxTotal = kotItems.sumOf {
                if (it.taxType == "exclusive")
                    it.basePrice * it.quantity * (it.taxRate / 100)
                else 0.0
            }

            val now = System.currentTimeMillis()
            val orderId = UUID.randomUUID().toString()
            val outlet = outletDao.getOutlet() ?: error("Outlet not configured")

            val srno = orderSequenceRepository.nextOrderNo(
                outletId = outlet.outletId,
                businessDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            )

            val orderMaster = PosOrderMasterEntity(
                id = orderId,
                srno = srno,
                orderType = orderType,
                tableNo = if (orderType == "DINE_IN") tableId else null,
                customerName = deliveryAddress?.name ?: "Walk-in",
                customerPhone = deliveryAddress?.phone ?: "",
                dAddressLine1 = deliveryAddress?.line1,
                dAddressLine2 = deliveryAddress?.line2,
                dCity = deliveryAddress?.city,
                dState = deliveryAddress?.state,
                dZipcode = deliveryAddress?.zipcode,
                dLandmark = deliveryAddress?.landmark,
                itemTotal = itemSubtotal,
                taxTotal = taxTotal,
                discountTotal = 0.0,
                grandTotal = itemSubtotal + taxTotal,
                paymentType = paymentType,
                paymentStatus = "PAID",
                orderStatus = "COMPLETED",
                deviceId = "POS",
                deviceName = "POS",
                appVersion = "1.0",
                createdAt = now,
                updatedAt = now,
                syncStatus = "PENDING",
                lastSyncedAt = null,
                notes = null
            )

            val orderItems = kotItems
                .groupBy { Triple(it.productId, it.basePrice, it.taxRate) }
                .map { (_, group) ->
                    val first = group.first()
                    val quantity = group.sumOf { it.quantity }
                    val subtotal = first.basePrice * quantity
                    val taxPerItem =
                        if (first.taxType == "exclusive") first.basePrice * (first.taxRate / 100)
                        else 0.0
                    val taxTotalItem = taxPerItem * quantity
                    PosOrderItemEntity(
                        id = UUID.randomUUID().toString(),
                        orderMasterId = orderId,
                        productId = first.productId,
                        name = first.name,
                        categoryId = first.categoryId,
                        parentId = first.parentId,
                        isVariant = first.isVariant,
                        basePrice = first.basePrice,
                        quantity = quantity,
                        itemSubtotal = subtotal,
                        taxRate = first.taxRate,
                        taxType = first.taxType,
                        taxAmountPerItem = taxPerItem,
                        taxTotal = taxTotalItem,
                        finalPricePerItem = first.basePrice + taxPerItem,
                        finalTotal = subtotal + taxTotalItem,
                        createdAt = now
                    )
                }

            // Save order and items atomically
            withContext(Dispatchers.IO) {
                orderMasterDao.insert(orderMaster)
                orderProductDao.insertAll(orderItems)
                kotItemDao.clearForTable(tableId)
            }

            // Print and finish
            printOrder(orderMaster, orderItems)
        }
    }

    // --------------------------------------------------------
    // Set Delivery Address
    // --------------------------------------------------------
    fun setDeliveryAddress(address: DeliveryAddressUiState) {
        _deliveryAddress.value = address
    }

    // --------------------------------------------------------
    // Printing (Unified print pipeline)
    // --------------------------------------------------------
    private suspend fun printOrder(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ) = withContext(Dispatchers.IO) {
        val printOrder = PrintOrderBuilder.build(order, items)
        val outlet = outletDao.getOutlet()

        val outletTitle = outlet?.let {
            listOfNotNull(
                it.outletName.takeIf { it.isNotBlank() },
                it.addressLine1.takeIf { it.isNotBlank() },
                it.addressLine2?.takeIf { it.isNotBlank() },
                it.addressLine3?.takeIf { it.isNotBlank() },
                it.city.takeIf { it.isNotBlank() },
                it.phone.takeIf { it.isNotBlank() }?.let { p -> "Contact No.: $p" },
                it.phone2?.takeIf { it.isNotBlank() },
                it.email?.takeIf { it.isNotBlank() },
                it.web?.takeIf { it.isNotBlank() },
                it.footerNote?.takeIf { it.isNotBlank() },
                it.gstVatNumber?.takeIf { it.isNotBlank() }?.let { gst -> "GST: $gst" }
            ).joinToString("\n")
        } ?: "FOOD APP"

        val receiptText = ReceiptFormatter.billing(printOrder, title = outletTitle)
        printerManager.printText(PrinterRole.BILLING, receiptText)
        Log.d("PRINT_ORDER", "Receipt printed successfully | orderNo=${order.srno}")
    }


    // -------------------------
    // PLACE ORDER + AUTO PRINT
    // -------------------------
    fun sendToKitchen(
        orderType: String,
        tableNo: String?,
        sessionId: String,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ) {
        Log.d("KITCHEN_DEBUG", "Start placeOrder() | tableNo=$tableNo orderType=$orderType")

        viewModelScope.launch {
            _loading.value = true

            // ✅ use sessionId as the real key for cart & KOT
            val sessionKey = sessionId
            Log.d("KITCHEN_DEBUG", "Resolved sessionKey=$sessionKey")

            // ✅ FIX: Use sessionKey (for takeaway & delivery)
            val cartList = repository.getCartItems(sessionKey, orderType).first()
            Log.d("KITCHEN_DEBUG", "Cart fetched for type=$orderType, sessionKey=$sessionKey, size=${cartList.size}")

            if (cartList.isEmpty()) {
                Log.w("KITCHEN_DEBUG", "⚠️ No new items found for orderType=$orderType (sessionKey=$sessionKey)")
                _loading.value = false
                return@launch
            }

            try {
                val now = System.currentTimeMillis()
                val orderId = UUID.randomUUID().toString()

                Log.d("KOT_STEP", "Creating new KOT batchId=$orderId for $orderType")

                val kotSaved = saveKotOnly(
                    orderType = orderType,
                    tableNo = sessionKey,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion
                )

                if (!kotSaved) {
                    Log.e("KITCHEN_DEBUG", "❌ saveKotOnly() failed for session=$sessionKey")
                    return@launch
                }

                Log.d("KITCHEN_DEBUG", "✅ KOT saved successfully (${cartList.size} items)")

                // ✅ FIX: clear by sessionKey (not tableNo)
                Log.d("KITCHEN_DEBUG", "Clearing cart for sessionKey=$sessionKey")
                repository.clearCart(orderType, sessionKey)
                Log.d("KITCHEN_DEBUG", "✅ Cart cleared for sessionKey=$sessionKey")

            } catch (e: Exception) {
                Log.e("KITCHEN_DEBUG", "💥 Exception during placeOrder()", e)
            } finally {
                _loading.value = false
            }
        }
    }


    private suspend fun saveKotOnly(
        orderType: String,
        tableNo: String?,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ): Boolean {
        return try {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val batchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            repository.markAllSent(tableNo ?: orderType)
            //  Log.d("KOT_STEP", "Marked ${items.size} items as sent to kitchen")
            val batch = PosKotBatchEntity(
                id = batchId,
                tableNo = tableNo ?: orderType,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                createdAt = now,
                sentBy = null,
                syncStatus = "PENDING",
                lastSyncedAt = null
            )

            withContext(Dispatchers.IO) {
                kotBatchDao.insert(batch)
                Log.d("KOT_DEBUG", "Saved ${cartItems.size} KOT items for tableNo=${tableNo ?: orderType}")
                val items = cartItems.map { cart ->
                    PosKotItemEntity(
                        id = UUID.randomUUID().toString(),
                        kotBatchId = batchId,
                        tableNo = tableNo ?: orderType,
                        productId = cart.productId,
                        name = cart.name,
                        categoryId = cart.categoryId,
                        parentId = cart.parentId,
                        isVariant = cart.isVariant,
                        basePrice = cart.basePrice,
                        quantity = cart.quantity,
                        taxRate = cart.taxRate,
                        taxType = cart.taxType,
                        isPrinted = false,
                        status = "PENDING",   // ✅ REQUIRED
                        createdAt = now
                    )
                }

                kotItemDao.insertAll(items)
            }

            Log.d("KOT", "✅ KOT SAVED: batch=$batchId items=${cartItems.size}")
            true

        } catch (e: Exception) {
            Log.e("KOT", "❌ Failed to save KOT", e)
            false
        }
    }


}
