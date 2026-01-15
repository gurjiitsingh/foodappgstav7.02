package com.it10x.foodappgstav7_02.ui.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.local.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.local.usecase.KotToBillUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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




    fun markDone(itemId: String) {
        viewModelScope.launch {
            kotToBillUseCase.markDoneAndMerge(itemId)
            Log.d("KOT_STEP", "KOT item $itemId DONE & merged to Bill")
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
