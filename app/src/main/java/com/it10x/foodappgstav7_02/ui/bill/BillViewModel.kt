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
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_02.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository, // ✅ ADD
    private val outletDao: OutletDao,                              // ✅ ADD
    private val tableId: String,
    private val orderType: String,
    private val printerManager: PrinterManager
) : ViewModel() {

    // =====================================================
// DELIVERY ADDRESS (UI → VM SNAPSHOT)
// =====================================================
    private val _deliveryAddress =
        MutableStateFlow<DeliveryAddressUiState?>(null)

    val deliveryAddress: DeliveryAddressUiState?
        get() = _deliveryAddress.value
    private val _uiState = MutableStateFlow(BillUiState(loading = true))

    val orderTypePublic: String
        get() = orderType
    val uiState: StateFlow<BillUiState> = _uiState

    init {
        Log.d("BILL_STEP", "BillViewModel init | table=$tableId")
        observeBill()
    }


    // 🔹 POS business date (API 24 safe)
    private fun businessDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
    }
    private fun observeBill() {
        viewModelScope.launch {
            Log.d("BILL_STEP", "Observing KOT items for table=$tableId")

            kotItemDao
                .getItemsForTable(tableId) // ✅ TABLE-WISE
                .collectLatest { kotItems ->

                    Log.d(
                        "BILL_STEP",
                        "Fetched KOT items | table=$tableId total=${kotItems.size}"
                    )

                    val doneItems = kotItems.filter { it.status == "DONE" }

                    Log.d(
                        "BILL_STEP",
                        "DONE items | table=$tableId count=${doneItems.size}"
                    )

                    val billingItems = doneItems
                        .groupBy { it.productId }
                        .map { (_, group) ->
                            val first = group.first()
                            val quantity = group.sumOf { it.quantity }

                            Log.d(
                                "BILL_STEP",
                                "Billing item | table=$tableId product=${first.name} qty=$quantity rows=${group.size}"
                            )

                            val subtotal = group.sumOf {
                                it.basePrice * it.quantity
                            }

                            val taxTotal = group.sumOf {
                                if (it.taxType == "exclusive") {
                                    it.basePrice * it.quantity * (it.taxRate / 100)
                                } else 0.0
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

                    Log.d(
                        "BILL_STEP",
                        "Bill updated | table=$tableId items=${billingItems.size} subtotal=$subtotal tax=$tax"
                    )

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

    fun payBill(paymentType: String) {
        viewModelScope.launch {

            // 1️⃣ Read DONE KOT items
            val kotItems = kotItemDao
                .getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }

            Log.d(
                "ORDER_SRNO",
                "payBill() | table=$tableId DONE items=${kotItems.size}"
            )

            if (kotItems.isEmpty()) return@launch

            // 2️⃣ Calculate totals
            val itemSubtotal = kotItems.sumOf { it.basePrice * it.quantity }
            val taxTotal = kotItems.sumOf {
                if (it.taxType == "exclusive") {
                    it.basePrice * it.quantity * (it.taxRate / 100)
                } else 0.0
            }

            val now = System.currentTimeMillis()
            val orderId = UUID.randomUUID().toString()

            // 3️⃣ Get outlet (SAFE)
            val outlet = outletDao.getOutlet()
                ?: throw IllegalStateException("Outlet not configured")

            // 4️⃣ Generate ATOMIC SR NO (POS STANDARD)
            val srno = orderSequenceRepository.nextOrderNo(
                outletId = outlet.outletId,
                businessDate = java.text.SimpleDateFormat(
                    "yyyyMMdd",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())
            )

            Log.d(
                "ORDER_SRNO",
                "Generated srno=$srno outletId=${outlet.outletId}"
            )

            // 5️⃣ Insert OrderMaster
//            val orderMaster = PosOrderMasterEntity(
//                id = orderId,
//                srno = srno,
//                orderType = orderType,
//                tableNo = if (orderType == "DINE_IN") tableId else null,
//                itemTotal = itemSubtotal,
//                taxTotal = taxTotal,
//                discountTotal = 0.0,
//                grandTotal = itemSubtotal + taxTotal,
//                paymentType = paymentType,
//                paymentStatus = "PAID",
//                orderStatus = "COMPLETED",
//                deviceId = "POS",
//                deviceName = "POS",
//                appVersion = "1.0",
//                createdAt = now,
//                updatedAt = now,
//                syncStatus = "PENDING",
//                lastSyncedAt = null,
//                notes = null
//            )

            val orderMaster = PosOrderMasterEntity(
                id = orderId,
                srno = srno,
                orderType = orderType,
                tableNo = if (orderType == "DINE_IN") tableId else null,
                customerName = deliveryAddress?.name,
                customerPhone = deliveryAddress?.phone,
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

            orderMasterDao.insert(orderMaster)

            Log.d(
                "ORDER_SRNO",
                "Order saved | orderId=$orderId srno=$srno"
            )

            // 6️⃣ Insert OrderItems (FINAL SNAPSHOT)
            val orderItems = kotItems
                .groupBy {
                    Triple(it.productId, it.basePrice, it.taxRate)
                }
                .map { (_, group) ->

                    val first = group.first()
                    val quantity = group.sumOf { it.quantity }

                    val subtotal = first.basePrice * quantity
                    val taxPerItem =
                        if (first.taxType == "exclusive")
                            first.basePrice * (first.taxRate / 100)
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

            orderProductDao.insertAll(orderItems)


            printOrderStandard(orderMaster, orderItems)



            // 7️⃣ Clear KOT
            kotItemDao.clearForTable(tableId)

            Log.d(
                "ORDER_SRNO",
                "Payment completed | table=$tableId orderId=$orderId srno=$srno"
            )
        }
    }

    fun setDeliveryAddress(address: DeliveryAddressUiState) {
        _deliveryAddress.value = address
    }




    // -------------------------
    // PRINT ORDERS (AUTO + MANUAL)
    // -------------------------
    private fun printOrderStandard(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ) {
        Log.d("PRINT_SOURCE", "🟢 POSOrdersViewModel.printOrderStandard CALLED")

        viewModelScope.launch {

            //  Log.d("OUTLET_PRINT", "📨 Building PrintOrder…")

            val printOrder = PrintOrderBuilder.build(order, items)

            // ---------------- OUTLET FROM ROOM ----------------
            val db = AppDatabaseProvider.get(printerManager.appContext())
            //    Log.d("OUTLET_DB_PRINT", "DB Path Print = ${db.openHelper.readableDatabase.path}")

            //    Log.d("OUTLET_PRINT", "🔍 Fetching outlet from Room…")

            val outlet = withContext(Dispatchers.IO) {
                db.outletDao().getOutlet()
            }

            if (outlet == null) {
                Log.e("OUTLET_PRINT", "❌ Outlet is NULL — using default title")
            } else {
                //  Log.d("OUTLET_PRINT", "✅ Outlet Loaded")
                //   Log.d("OUTLET_PRINT", "name=${outlet.outletName}")
                //   Log.d("OUTLET_PRINT", "city=${outlet.city}")
                //   Log.d("OUTLET_PRINT", "phone=${outlet.phone}")
            }

            val outletTitle = if (outlet != null) {
                listOfNotNull(
                    outlet.outletName.takeIf { it.isNotBlank() },

                    // ADDRESS
                    outlet.addressLine1.takeIf { it.isNotBlank() },
                    outlet.addressLine2?.takeIf { it.isNotBlank() },
                    outlet.addressLine3?.takeIf { it.isNotBlank() },
                    outlet.city.takeIf { it.isNotBlank() },

                    // CONTACT
                    outlet.phone.takeIf { it.isNotBlank() }?.let { "Contact No.: $it" },
                    outlet.phone2?.takeIf { it.isNotBlank() }?.let { "$it" },
                    outlet.email?.takeIf { it.isNotBlank() },

                    // WEB
                    outlet.web?.takeIf { it.isNotBlank() },

                    // FOOTER NOTE
                    outlet.footerNote?.takeIf { it.isNotBlank() },
                    // TAX
                    outlet.gstVatNumber?.takeIf { it.isNotBlank() }?.let { "GST: $it" }
                ).joinToString("\n")
            } else {
                "FOOD APP"
            }

            //oldprintingremoved
            //  Log.d("OUTLET_PRINT", "🖨 FINAL TITLE:\n$outletTitle")

            // ---------------- BILLING PRINT ----------------
            printerManager.printText(
                PrinterRole.BILLING,
                ReceiptFormatter.billing(printOrder, title = outletTitle)
            )


        }
    }

}
