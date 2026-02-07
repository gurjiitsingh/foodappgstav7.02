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
import com.it10x.foodappgstav7_02.data.pos.repository.OutletRepository
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
import com.it10x.foodappgstav7_02.data.print.OutletInfo
import com.it10x.foodappgstav7_02.data.print.OutletMapper
import kotlinx.coroutines.flow.Flow

class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager,
    private val outletRepository: OutletRepository
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    private val _currencySymbol = MutableStateFlow("₹") // fallback
    val currencySymbol: StateFlow<String> = _currencySymbol

  //  val outletInfo: StateFlow<OutletInfo> = outletRepository.outletInfo
    // ✅ Expose orderType safely for Compose UI
    val orderTypePublic: String
        get() = orderType

    init {
        Log.d("BILL_INIT", "Initialized | table=$tableId")
        observeBill()
        loadCurrency()
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

                        val itemTotal = first.basePrice * quantity

                        val taxTotal = group.sumOf {
                            if (it.taxType == "exclusive")
                                it.basePrice * it.quantity * (it.taxRate / 100)
                            else 0.0
                        }

                        BillingItemUi(
                            id = first.productId,
                            name = first.name,
                            basePrice = first.basePrice,   // ✅ ONE item price
                            quantity = quantity,
                            itemtotal = itemTotal,         // ✅ qty × price
                            taxTotal = taxTotal,           // ✅ per-item tax
                            finalTotal = itemTotal + taxTotal
                        )
                    }

                val subtotal = billingItems.sumOf { it.itemtotal }
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


    private fun loadCurrency() {
        viewModelScope.launch {
            val outletInfo = outletRepository.getOutletInfo()
            _currencySymbol.value = outletInfo.defaultCurrency
        }
    }



    suspend fun hasPendingKitchenItems(): Boolean {
        return kotItemDao.countKitchenPending(tableId) > 0
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
        val outletInfo = OutletMapper.fromEntity(outlet)

        printerManager.printTextNew(PrinterRole.BILLING, printOrder)
        Log.d("PRINT_ORDER", "Receipt printed successfully | orderNo=${order.srno}")
    }


    fun getDoneItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {
        return kotItemDao.getDoneItemsForTable(orderRef)
    }


}
