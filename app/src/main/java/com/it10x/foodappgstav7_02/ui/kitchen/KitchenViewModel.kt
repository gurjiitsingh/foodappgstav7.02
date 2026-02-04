package com.it10x.foodappgstav7_02.ui.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_02.printer.PrintItem
import com.it10x.foodappgstav7_02.printer.PrintOrder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class KitchenViewModel(
    app: Application,
    private val tableId: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val cartViewModel: CartViewModel,
) : AndroidViewModel(app) {

    private var kotPrintJob: Job? = null
    private val pendingKotItems = mutableListOf<PosKotItemEntity>()
    private var pendingBatchId: String? = null
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()


    private val kotToBillUseCase =
        KotToBillUseCase(kotItemDao)

    val kotItems: StateFlow<List<PosKotItemEntity>> =
        kotItemDao.getAllKotItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val printerManager =
        PrinterManager(app.applicationContext)
//USE SESSION ID TO FETCH DATA
    fun getKotItemsForTable(tableNo: String): StateFlow<List<PosKotItemEntity>> {
        val state = MutableStateFlow<List<PosKotItemEntity>>(emptyList())

        viewModelScope.launch {
            kotItemDao
                .getItemsForTable(tableNo)
                .collect { items ->
                    state.value = items
                }
        }
        return state
    }


    fun markDone(itemId: String,orderType: String,  print: Boolean = true) {
        viewModelScope.launch {

            kotToBillUseCase.markDoneAndMerge(itemId)

            val item = kotItemDao.getItemByIdSync(itemId) ?: return@launch

            // ❌ If already printed → DO NOT PRINT AGAIN
            if (item.isPrinted || !print) return@launch

            printerManager.printTextKitchen(
                PrinterRole.KITCHEN,
                sessionKey = item.tableNo ?: item.kotBatchId,
                orderType = orderType,
                items = listOf(item)
                )

            kotItemDao.markPrinted(item.id)

            Log.d("KITCHEN_PRINT", "Printed single item ${item.name}")
        }
    }



    fun markDoneAll(orderType: String, tableNo: String) {
        viewModelScope.launch {

            val unprintedItems = kotItemDao.getUnprintedItems(tableNo)
            if (unprintedItems.isEmpty()) return@launch

            // 🔥 PRINT ONCE (ALL ITEMS)

            printerManager.printTextKitchen(
                PrinterRole.KITCHEN,
                sessionKey = tableNo,
                orderType = orderType,
                items = unprintedItems)

            // ✅ MARK ALL
            kotItemDao.markAllDone(tableNo)
            kotItemDao.markAllPrinted(tableNo)

            Log.d("KITCHEN_PRINT", "Done All printed for table=$tableNo")
        }
    }




    fun markCancelled(itemId: String) {
        viewModelScope.launch {
            kotItemDao.updateStatus(itemId, "CANCELLED")
        }
    }


    fun getPendingItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {


        return if (orderType == "DINE_IN") {
            kotItemDao.getPendingItemsForTable(orderRef)
        } else {
            kotItemDao.getPendingItemsForTable(orderType)
          //  kotItemDao.getPendingItemsForSession(orderRef)
        }
    }



    fun logAllKotItems() {
        viewModelScope.launch {
            kotItemDao.getTotalKotItems()
                .collect { items ->
                    Log.d("KITCHEN_DEBUG2", "Total items = ${items.size}")

                    items.forEach { item ->
                        Log.d(
                            "KITCHEN_DEBUG1",
                            "Status=${item.status},Table=${item.tableNo}, session=${item.sessionId}, BatchId=${item.kotBatchId},Name=${item.name},ID=${item.id}"
                        )
                    }
                }
        }
    }

    fun deleteAllKotItems() {
        viewModelScope.launch {
            kotItemDao.deleteAllKotItems()
            Log.d("KITCHEN_DEBUG", "All KOT items deleted")
        }
    }


    // ✅ POS signal: kitchen completed for table
    fun isKitchenEmptyForTable(tableNo: String): StateFlow<Boolean> {
        return kotItemDao.getItemsForTable(tableNo)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
            .let { flow ->
                MutableStateFlow(false).also { state ->
                    viewModelScope.launch {
                        flow.collect { items ->
                            state.value = items.isEmpty()
                        }
                    }
                }
            }
    }



    fun sendToKitchen(
        orderType: String,
        tableNo: String?,
        sessionId: String,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?
    ) {
        Log.d("KITCHEN_DEBUG4", "sendToKitchen tableNo=$tableNo orderType=$orderType sessionId=$sessionId ")
        logAllKotItems()
        viewModelScope.launch {
            _loading.value = true

            // ✅ use sessionId as the real key for cart & KOT
            val sessionKey = sessionId
       //     Log.d("KITCHEN_DEBUG", "Resolved sessionKey=$sessionKey")

            // ✅ FIX: Use sessionKey (for takeaway & delivery)
            //val cartList = repository.getCartItems(sessionKey, orderType).first()
            val cartList = repository.getCartItems(sessionKey).first()
            //Log.d("KITCHEN_DEBUG", "Cart fetched for type=$orderType, sessionKey=$sessionKey, size=${cartList.size}")

            if (cartList.isEmpty()) {
                Log.w("KITCHEN_DEBUG4", "⚠️ No new items found for orderType=$orderType (sessionKey=$sessionKey)")
                _loading.value = false
                return@launch
            }

            try {
                val now = System.currentTimeMillis()
                val orderId = UUID.randomUUID().toString()

                Log.d("KITCHEN_DEBUG4", "Creating new KOT batchId=$orderId for $orderType")

                val kotSaved = saveKotOnly(
                    orderType = orderType,
                    sessionId = sessionId,
                    tableNo = tableNo,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion
                )

                if (!kotSaved) {
                   Log.e("KITCHEN_DEBUG4", " saveKotOnly() failed for session=$sessionKey")
                    return@launch
                }

                Log.d("KITCHEN_DEBUG4", " KOT saved successfully (${cartList.size} items)")

                //  FIX: clear by sessionKey (not tableNo)
              //  Log.d("KITCHEN_DEBUG", "Clearing cart for sessionKey=$sessionKey")
                repository.clearCart(orderType, sessionKey)
              //  Log.d("KITCHEN_DEBUG", " Cart cleared for sessionKey=$sessionKey")

            } catch (e: Exception) {
              //  Log.e("KITCHEN_DEBUG", " Exception during placeOrder()", e)
            } finally {
                _loading.value = false
            }
        }
    }


    private suspend fun saveKotOnly(
        orderType: String,
        sessionId: String,
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
                sessionId = sessionId,
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
            //    Log.d("KOT_DEBUG", "Saved ${cartItems.size} KOT items for tableNo=${tableNo ?: orderType}")
                val items = cartItems.map { cart ->
                    PosKotItemEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
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



    fun sendSingleItemDirectlyToBill(
        cart: PosCartEntity,
        orderType: String,
        tableNo: String?,
        sessionId: String,
        print: Boolean
    ) {

//        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//        val deviceName = Build.MODEL ?: "Unknown Device"
//        val appVersion = BuildConfig.VERSION_NAME

        viewModelScope.launch(Dispatchers.IO) {

            val db = AppDatabaseProvider.get(getApplication())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val now = System.currentTimeMillis()
            val batchId = UUID.randomUUID().toString()

            // 🔹 Create batch (required for consistency)
            val batch = PosKotBatchEntity(
                id = batchId,
                sessionId = sessionId,
                tableNo = tableNo ?: orderType,
                orderType = orderType,
                deviceId = "dummy",
                deviceName = "dummy",
                appVersion = "dummy",
                createdAt = now,
                sentBy = "dummy",
                syncStatus = "DONE",
                lastSyncedAt = null
            )

            kotBatchDao.insert(batch)

            // 🔹 Create SINGLE KOT item → DONE
            val kotItem = PosKotItemEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
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
                status = "DONE",
                isPrinted = false,
                createdAt = now
            )

            kotItemDao.insert(kotItem)

            // 🔹 Print if required
            if (print) {
                addItemToDebouncedKitchenPrint(kotItem, orderType)
                kotItemDao.markPrinted(kotItem.id)
            }


            // 🔹 Remove from cart after sending to bill
            cartViewModel.removeFromCart(cart.productId)


        }
    }


    private fun addItemToDebouncedKitchenPrint(
        item: PosKotItemEntity,
        orderType: String
    ) {
        synchronized(this) {
            pendingKotItems.add(item)
            if (pendingBatchId == null) {
                pendingBatchId = item.kotBatchId
            }
        }

        // Cancel previous timer
        kotPrintJob?.cancel()

        // Start / restart 10s timer
        kotPrintJob = viewModelScope.launch {
            delay(10_000) // ⏱️ 10 seconds

            val itemsToPrint: List<PosKotItemEntity>
            val batchId: String?

            synchronized(this@KitchenViewModel) {
                itemsToPrint = pendingKotItems.toList()
                batchId = pendingBatchId
                pendingKotItems.clear()
                pendingBatchId = null
            }

            if (itemsToPrint.isNotEmpty()) {
                printerManager.printTextKitchen(
                    PrinterRole.KITCHEN,
                    sessionKey = itemsToPrint.first().tableNo ?: batchId!!,
                    orderType = orderType,
                    items = itemsToPrint
                )

                // mark all printed
                val db = AppDatabaseProvider.get(getApplication())
                db.kotItemDao().markPrintedBatch(itemsToPrint.map { it.id })
            }
        }
    }



}





