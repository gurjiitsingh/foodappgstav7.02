package com.it10x.foodappgstav7_02.ui.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_02.printer.PrintItem
import com.it10x.foodappgstav7_02.printer.PrintOrder
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KitchenViewModel(
    app: Application
) : AndroidViewModel(app) {

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









    fun markDone(itemId: String, print: Boolean = true) {
        viewModelScope.launch {

            kotToBillUseCase.markDoneAndMerge(itemId)

            val item = kotItemDao.getItemByIdSync(itemId) ?: return@launch

            // ❌ If already printed → DO NOT PRINT AGAIN
            if (item.isPrinted || !print) return@launch

            val slip = ReceiptFormatter.posKitchen(
                sessionKey = item.tableNo ?: item.kotBatchId,
                orderType = "DINE_IN",
                items = listOf(item)
            )

            printerManager.printText(PrinterRole.KITCHEN, slip)

            kotItemDao.markPrinted(item.id)

            Log.d("KITCHEN_PRINT", "Printed single item ${item.name}")
        }
    }



    fun markDoneAll(tableNo: String) {
        viewModelScope.launch {

            val unprintedItems = kotItemDao.getUnprintedItems(tableNo)
            if (unprintedItems.isEmpty()) return@launch

            // 🔥 PRINT ONCE (ALL ITEMS)
            val slip = ReceiptFormatter.posKitchen(
                sessionKey = tableNo,
                orderType = "DINE_IN",
                items = unprintedItems
            )

            printerManager.printText(PrinterRole.KITCHEN, slip)

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

    fun getPendingItems(tableNo: String) =
        kotItemDao.getPendingItemsForTable(tableNo)


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



}





//    fun markDoneAll(tableNo: String) {
//        viewModelScope.launch {
//
//            val unprintedItems =
//                kotItemDao.getUnprintedPendingItems(tableNo)
//
//            if (unprintedItems.isNotEmpty()) {
//
//                val slip = ReceiptFormatter.posKitchen(
//                    sessionKey = tableNo,
//                    orderType = "DINE_IN",
//                    items = unprintedItems
//                )
//
//                printerManager.printText(
//                    PrinterRole.KITCHEN,
//                    slip
//                )
//
//                kotItemDao.markAllPrintedForTable(tableNo)
//            }
//
//            // Mark all pending items DONE
//            unprintedItems.forEach {
//                kotToBillUseCase.markDoneAndMerge(it.id)
//            }
//        }
//    }

//    fun markDone(itemId: String) {
//        viewModelScope.launch {
//
//            val item = kotItemDao.getItemByIdSync(itemId) ?: return@launch
//
//            // 1️⃣ Print ONLY if never printed
//            if (!item.isPrinted) {
//
//                val slip = ReceiptFormatter.posKitchen(
//                    sessionKey = item.tableNo ?: item.kotBatchId,
//                    orderType = item.tableNo?.let { "DINE_IN" } ?: "TAKEAWAY",
//                    items = listOf(item)   // ✅ SINGLE ITEM
//                )
//
//                printerManager.printText(
//                    PrinterRole.KITCHEN,
//                    slip
//                )
//
//                kotItemDao.markPrinted(item.id)
//            }
//
//            // 2️⃣ Business logic (unchanged)
//            kotToBillUseCase.markDoneAndMerge(itemId)
//
//            Log.d(
//                "KITCHEN_PRINT",
//                "Item done | printed=${item.isPrinted} | ${item.name}"
//            )
//        }
//    }