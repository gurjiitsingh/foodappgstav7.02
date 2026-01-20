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









    fun markDone(itemId: String,orderType: String,  print: Boolean = true) {
        viewModelScope.launch {

            kotToBillUseCase.markDoneAndMerge(itemId)

            val item = kotItemDao.getItemByIdSync(itemId) ?: return@launch

            // ❌ If already printed → DO NOT PRINT AGAIN
            if (item.isPrinted || !print) return@launch

            val slip = ReceiptFormatter.posKitchen(
                sessionKey = item.tableNo ?: item.kotBatchId,
                orderType = orderType,
                items = listOf(item)
            )

            printerManager.printText(PrinterRole.KITCHEN, slip)

            kotItemDao.markPrinted(item.id)

            Log.d("KITCHEN_PRINT", "Printed single item ${item.name}")
        }
    }



    fun markDoneAll(orderType: String, tableNo: String) {
        viewModelScope.launch {

            val unprintedItems = kotItemDao.getUnprintedItems(tableNo)
            if (unprintedItems.isEmpty()) return@launch

            // 🔥 PRINT ONCE (ALL ITEMS)
            val slip = ReceiptFormatter.posKitchen(
                sessionKey = tableNo,
                orderType = orderType,
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





