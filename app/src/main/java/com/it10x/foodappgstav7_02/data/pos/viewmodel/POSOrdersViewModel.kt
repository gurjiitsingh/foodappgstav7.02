package com.it10x.foodappgstav7_02.data.pos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.print.OutletMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// 🔹 NEW (for atomic order no + API 24 safe date)
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // 🔹 NEW: API-24 safe business date (yyyyMMdd)
    private fun businessDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
    }
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
            pagedOrders.forEach {
                Log.d("ORDER_SRNO", "Loaded order id=${it.id} srno=${it.srno}")
            }

            _orders.value = pagedOrders.sortedByDescending { it.createdAt }
            _loading.value = false
        }
    }

    // -------------------------
    // PLACE ORDER + AUTO PRINT
    // -------------------------
//    fun sendToKitchen(
//        orderType: String,
//        tableNo: String?,
//        sessionId: String,
//        paymentType: String,
//        deviceId: String,
//        deviceName: String?,
//        appVersion: String?
//    ) {
//        Log.d("KITCHEN_DEBUG", "Start placeOrder() | tableNo=$tableNo orderType=$orderType")
//
//        viewModelScope.launch {
//            _loading.value = true
//
//            // ✅ use sessionId as the real key for cart & KOT
//            val sessionKey = sessionId
//            Log.d("KITCHEN_DEBUG", "Resolved sessionKey=$sessionKey")
//
//            // ✅ FIX: Use sessionKey (for takeaway & delivery)
//            val cartList = repository.getCartItems(sessionKey, orderType).first()
//            Log.d("KITCHEN_DEBUG", "Cart fetched for type=$orderType, sessionKey=$sessionKey, size=${cartList.size}")
//
//            if (cartList.isEmpty()) {
//                Log.w("KITCHEN_DEBUG", "⚠️ No new items found for orderType=$orderType (sessionKey=$sessionKey)")
//                _loading.value = false
//                return@launch
//            }
//
//            try {
//                val now = System.currentTimeMillis()
//                val orderId = UUID.randomUUID().toString()
//
//                Log.d("KOT_STEP", "Creating new KOT batchId=$orderId for $orderType")
//
//                val kotSaved = saveKotOnly(
//                    orderType = orderType,
//                    tableNo = sessionKey,
//                    cartItems = cartList,
//                    deviceId = deviceId,
//                    deviceName = deviceName,
//                    appVersion = appVersion
//                )
//
//                if (!kotSaved) {
//                    Log.e("KITCHEN_DEBUG", "❌ saveKotOnly() failed for session=$sessionKey")
//                    return@launch
//                }
//
//                Log.d("KITCHEN_DEBUG", "✅ KOT saved successfully (${cartList.size} items)")
//
//                // ✅ FIX: clear by sessionKey (not tableNo)
//                Log.d("KITCHEN_DEBUG", "Clearing cart for sessionKey=$sessionKey")
//                repository.clearCart(orderType, sessionKey)
//                Log.d("KITCHEN_DEBUG", "✅ Cart cleared for sessionKey=$sessionKey")
//
//            } catch (e: Exception) {
//                Log.e("KITCHEN_DEBUG", "💥 Exception during placeOrder()", e)
//            } finally {
//                _loading.value = false
//            }
//        }
//    }






    // -------------------------
    // AUTO PRINT
    // -------------------------
   // private fun autoPrint(order: PosOrderMasterEntity, cartItems: List<PosCartEntity>) {
//    private suspend fun autoPrint(
//        order: PosOrderMasterEntity,
//        cartItems: List<PosCartEntity>
//    ): Boolean {
//        return try {
//
//            // Convert cart → order items
//            val items = cartItems.map { cart ->
//                PosOrderItemEntity(
//                    id = UUID.randomUUID().toString(),
//                    orderMasterId = order.id,
//                    productId = cart.productId,
//                    categoryId = cart.categoryId,
//                    parentId = cart.parentId,
//                    isVariant = cart.isVariant,
//                    name = cart.name,
//                    quantity = cart.quantity,
//                    basePrice = cart.basePrice,
//                    itemSubtotal = cart.basePrice * cart.quantity,
//                    taxRate = cart.taxRate,
//                    taxType = cart.taxType,
//                    taxAmountPerItem = 0.0,
//                    taxTotal = 0.0,
//                    finalPricePerItem = cart.basePrice,
//                    finalTotal = cart.basePrice * cart.quantity,
//                    createdAt = System.currentTimeMillis()
//                )
//            }
//
//            // 🔥 SUSPEND call (waits until print finishes)
//            printOrderStandard(order, items)
//
//            true   // ✅ print success
//
//        } catch (e: Exception) {
//            Log.e("AUTO_PRINT", "Printing failed", e)
//            false  // ❌ print failed
//        }
//    }


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



          // ---------------- BILLING PRINT ----------------



            printerManager.printTextNew(PrinterRole.BILLING, printOrder)

            // SMALL DELAY
            kotlinx.coroutines.delay(150)

            // ---------------- KITCHEN PRINT ----------------
            printerManager.printTextNew(
                PrinterRole.KITCHEN,
                printOrder
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
                    Log.d(
                        "ORDER_SRNO",
                        "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
                    )
                    return@launch
                }

                Log.d(
                    "ORDER_SRNO",
                    "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
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
