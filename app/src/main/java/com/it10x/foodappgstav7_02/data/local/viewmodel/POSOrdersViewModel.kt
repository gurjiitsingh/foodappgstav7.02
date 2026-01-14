package com.it10x.foodappgstav7_02.data.local.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.local.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import com.it10x.foodappgstav7_02.data.local.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.local.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosKotItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class POSOrdersViewModel(
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    val orders: StateFlow<List<PosOrderMasterEntity>> get() = _orders
    private val _orders = MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())

    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    val pageIndex = MutableStateFlow(0)
    private val limit = 10
    private val srNoCounter = AtomicInteger(1)

    // -------------------------
    // PAGINATION
    // -------------------------
    fun loadFirstPage() = loadOrders(0)
    fun loadNextPage() = loadOrders(pageIndex.value + 1)
    fun loadPrevPage() {
        val prev = if (pageIndex.value > 0) pageIndex.value - 1 else 0
        loadOrders(prev)
    }

    private fun loadOrders(page: Int) {
        viewModelScope.launch {
            _loading.value = true
            pageIndex.value = page
            val offset = page * limit
            val pagedOrders = repository.getPagedOrders(limit, offset)
            _orders.value = pagedOrders.sortedByDescending { it.createdAt }
            _loading.value = false
        }
    }

    // -------------------------
    // PLACE ORDER + AUTO PRINT
    // -------------------------
    fun placeOrder(
        orderType: String,
        tableNo: String?,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ) {
        viewModelScope.launch {
            _loading.value = true

            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val cartList = repository.getUnsentItems(tableNo).first()

            if (cartList.isEmpty()) {
                Log.d("KOT", "No new items to send to kitchen")
                _loading.value = false
                return@launch
            }

            try {
                val now = System.currentTimeMillis()
                val orderId = UUID.randomUUID().toString()
                val srno = srNoCounter.getAndIncrement()

                // 2️⃣ Totals
                val itemTotal = OrderCalculator.subtotal(cartList)
                val taxTotal = OrderCalculator.tax(cartList)
                val grandTotal = OrderCalculator.grandTotal(cartList)

//                // 3️⃣ Master
//                val orderMaster = PosOrderMasterEntity(
//                    id = orderId,
//                    srno = srno,
//                    orderType = orderType,
//                    tableNo = tableNo,
//                    itemTotal = itemTotal,
//                    taxTotal = taxTotal,
//                    discountTotal = 0.0,
//                    grandTotal = grandTotal,
//                    paymentType = paymentType,
//                    paymentStatus = "PAID",
//                    orderStatus = "NEW",
//                    source = "POS",
//                    deviceId = deviceId,
//                    deviceName = deviceName,
//                    appVersion = appVersion,
//                    createdAt = now,
//                    updatedAt = now,
//                    syncStatus = "PENDING",
//                    lastSyncedAt = null,
//                    notes = null
//                )
//
//                repository.insertOrder(orderMaster, cartList)

                // 4️⃣ Print KOT → WAIT → THEN mark sent
//                val printed = autoPrint(orderMaster, cartList)

//                val printed =true;
//                if (printed) {
//
//                    val db = AppDatabaseProvider.get(printerManager.appContext())
//                    val kotBatchDao = db.kotBatchDao()
//                    val kotItemDao = db.kotItemDao()
//
//                    val kotBatchId = UUID.randomUUID().toString()
//
//                    val kotBatch = PosKotBatchEntity(
//                        id = kotBatchId,
//                        tableNo = tableNo,
//                        orderType = orderType,
//                        deviceId = deviceId,
//                        deviceName = deviceName,
//                        appVersion = appVersion,
//                        createdAt = System.currentTimeMillis(),
//                        sentBy = null,
//                        syncStatus = "PENDING",
//                        lastSyncedAt = null
//                    )
//
//                    withContext(Dispatchers.IO) {
//                        kotBatchDao.insert(kotBatch)
//
//                        val kotItems = cartList.map { cart ->
//                            PosKotItemEntity(
//                                id = UUID.randomUUID().toString(),
//                                kotBatchId = kotBatchId,
//                                tableNo = tableNo,
//                                productId = cart.productId,
//                                name = cart.name,
//                                categoryId = cart.categoryId,
//                                parentId = cart.parentId,
//                                isVariant = cart.isVariant,
//                                basePrice = cart.basePrice,
//                                quantity = cart.quantity,
//                                taxRate = cart.taxRate,
//                                taxType = cart.taxType,
//                                createdAt = System.currentTimeMillis()
//                            )
//                        }
//
//                        kotItemDao.insertAll(kotItems)
//                    }
//
//                    if (tableNo != null) {
//                        repository.markAllSent(tableNo)
//                    }
//                }


                val kotSaved = saveKotOnly(
                    orderType = orderType,
                    tableNo = tableNo,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion
                )

                if (!kotSaved) {
                    Log.e("KOT", "❌ KOT SAVE FAILED")
                    return@launch
                }
                debugReadKot(tableNo!!)

            } catch (e: Exception) {
                Log.e("POS", "Error placing order", e)
            } finally {
                _loading.value = false
            }
        }
    }


    // -------------------------
    // AUTO PRINT
    // -------------------------
   // private fun autoPrint(order: PosOrderMasterEntity, cartItems: List<PosCartEntity>) {
    private suspend fun autoPrint(
        order: PosOrderMasterEntity,
        cartItems: List<PosCartEntity>
    ): Boolean {
        return try {

            // Convert cart → order items
            val items = cartItems.map { cart ->
                PosOrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderMasterId = order.id,
                    productId = cart.productId,
                    categoryId = cart.categoryId,
                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    name = cart.name,
                    quantity = cart.quantity,
                    basePrice = cart.basePrice,
                    itemSubtotal = cart.basePrice * cart.quantity,
                    taxRate = cart.taxRate,
                    taxType = cart.taxType,
                    taxAmountPerItem = 0.0,
                    taxTotal = 0.0,
                    finalPricePerItem = cart.basePrice,
                    finalTotal = cart.basePrice * cart.quantity,
                    createdAt = System.currentTimeMillis()
                )
            }

            // 🔥 SUSPEND call (waits until print finishes)
            printOrderStandard(order, items)

            true   // ✅ print success

        } catch (e: Exception) {
            Log.e("AUTO_PRINT", "Printing failed", e)
            false  // ❌ print failed
        }
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


          //  Log.d("OUTLET_PRINT", "🖨 FINAL TITLE:\n$outletTitle")

            // ---------------- BILLING PRINT ----------------
            printerManager.printText(
                PrinterRole.BILLING,
                ReceiptFormatter.billing(printOrder, title = outletTitle)
            )

            // SMALL DELAY
            kotlinx.coroutines.delay(150)

            // ---------------- KITCHEN PRINT ----------------
            printerManager.printText(
                PrinterRole.KITCHEN,
                ReceiptFormatter.kitchen(printOrder)
            )
        }
    }






    // -------------------------
    // ORDER DETAILS
    // -------------------------
    fun getOrderProducts(orderId: String): StateFlow<List<PosOrderItemEntity>> {
        val flow = MutableStateFlow<List<PosOrderItemEntity>>(emptyList())
        viewModelScope.launch {
            flow.value = repository.getOrderItems(orderId)
        }
        return flow
    }

    // -------------------------
    // MANUAL PRINT OLD ORDER
    // -------------------------
    fun printOrder(orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {

              //  Log.d("POS_PRINT", "Print requested for orderId=$orderId")

                val order = repository.getOrderById(orderId)
                if (order == null) {
                    Log.e("POS_PRINT", "Order NOT FOUND for orderId=$orderId")
                    return@launch
                }

                val items = repository.getOrderItems(orderId)
                if (items.isEmpty()) {
                    Log.e(
                        "POS_PRINT",
                        "Order items EMPTY for orderId=$orderId (items=${items.size})"
                    )
                    return@launch
                }

                Log.d(
                    "POS_PRINT",
                    "Printing order srno=${order.srno}, items=${items.size}"
                )

                printOrderStandard(order, items)

            } catch (e: Exception) {
                Log.e("POS_PRINT", "Exception while printing order", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // -------------------------
// REQUEST BILL (NO PAYMENT)
// -------------------------
    fun requestBill(tableNo: String) {
        viewModelScope.launch {
            try {
                repository.markTableBillRequested(tableNo)
                Log.d("POS", "Bill requested for table=$tableNo")
            } catch (e: Exception) {
                Log.e("POS", "Failed to request bill", e)
            }
        }
    }

    // -------------------------
// PAY & CLOSE TABLE
// -------------------------
    fun payAndCloseTable(
        tableNo: String,
        paymentType: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val db = AppDatabaseProvider.get(printerManager.appContext())
                val kotItemDao = db.kotItemDao()
                val orderMasterDao = db.orderMasterDao()
                val orderProductDao = db.orderProductDao() // ✅ correct DAO
                val tableDao = db.tableDao()

                // 1️⃣ Read DONE KOT items
                val kotItems = kotItemDao.getItemsForTableSync(tableNo)
                    .filter { it.status == "DONE" }

                if (kotItems.isEmpty()) {
                    Log.e("BILL", "No DONE KOT items for table=$tableNo")
                    return@launch
                }

                // 2️⃣ Calculate totals (tax set default 0.0)
                val itemTotal = kotItems.sumOf { it.basePrice * it.quantity }
                val taxTotal = kotItems.sumOf { 0.0 } // default
                val grandTotal = itemTotal + taxTotal

                // 3️⃣ Create OrderMaster
                val orderId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()

                val master = PosOrderMasterEntity(
                    id = orderId,
                    srno = srNoCounter.getAndIncrement(),
                    orderType = "DINE_IN",
                    tableNo = tableNo,
                    itemTotal = itemTotal,
                    taxTotal = taxTotal,
                    discountTotal = 0.0,
                    grandTotal = grandTotal,
                    paymentType = paymentType,
                    paymentStatus = "PAID",
                    orderStatus = "COMPLETED",
                    source = "POS",
                    deviceId = "UNKNOWN",
                    deviceName = "UNKNOWN",
                    appVersion = "UNKNOWN",
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = "PENDING",
                    lastSyncedAt = null,
                    notes = null
                )

                orderMasterDao.insert(master)

                // 4️⃣ Copy KOT → OrderItems
                val orderItems = kotItems.map { kot ->
                    PosOrderItemEntity(
                        id = UUID.randomUUID().toString(),
                        orderMasterId = orderId,
                        productId = kot.productId,
                        name = kot.name,
                        categoryId = kot.categoryId,
                        parentId = kot.parentId,
                        isVariant = kot.isVariant,
                        basePrice = kot.basePrice,
                        quantity = kot.quantity,
                        itemSubtotal = kot.basePrice * kot.quantity,
                        taxRate = kot.taxRate,
                        taxType = kot.taxType,
                        taxAmountPerItem = 0.0,
                        taxTotal = 0.0,
                        finalPricePerItem = kot.basePrice,
                        finalTotal = kot.basePrice * kot.quantity,
                        createdAt = now
                    )
                }

                orderProductDao.insertAll(orderItems)

                // 5️⃣ Print FINAL BILL (optional - keep same)
                val printOrder = PrintOrderBuilder.build(master, orderItems)
                printerManager.printText(
                    PrinterRole.BILLING,
                    ReceiptFormatter.billing(printOrder)
                )

                // 6️⃣ Clear KOT
                kotItemDao.clearForTable(tableNo)

                // 7️⃣ Close table
                tableDao.closeTable(tableNo)

                Log.d("BILL", "✅ Payment completed table=$tableNo")

            } catch (e: Exception) {
                Log.e("BILL", "❌ Payment failed", e)
            } finally {
                _loading.value = false
            }
        }
    }



    // -------------------------
// CLOSE TABLE (NO BILLING)
// -------------------------
    fun closeTableOnly(tableNo: String) {
        viewModelScope.launch {
            try {
                repository.closeTable(tableNo)
                Log.d("POS", "Table marked AVAILABLE: $tableNo")
            } catch (e: Exception) {
                Log.e("POS", "Failed to close table", e)
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

            val batch = PosKotBatchEntity(
                id = batchId,
                tableNo = tableNo,
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

                val items = cartItems.map { cart ->
                    PosKotItemEntity(
                        id = UUID.randomUUID().toString(),
                        kotBatchId = batchId,
                        tableNo = tableNo,
                        productId = cart.productId,
                        name = cart.name,
                        categoryId = cart.categoryId,
                        parentId = cart.parentId,
                        isVariant = cart.isVariant,
                        basePrice = cart.basePrice,
                        quantity = cart.quantity,
                        taxRate = cart.taxRate,
                        taxType = cart.taxType,
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

    fun debugReadKot(tableNo: String) {
        viewModelScope.launch {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val batches = db.kotBatchDao().getBatchesForTable(tableNo).first()
            val items = db.kotItemDao().getItemsForTable(tableNo).first()

            Log.d("KOT_READ", "Batches=${batches.size}")
            Log.d("KOT_READ", "Items=${items.size}")

            items.forEach {
                Log.d("KOT_ITEM", "${it.name} x${it.quantity}")
            }
        }
    }

}
