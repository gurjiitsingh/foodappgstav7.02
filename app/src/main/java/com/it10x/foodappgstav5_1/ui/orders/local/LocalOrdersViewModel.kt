package com.it10x.foodappgstav5_1.ui.orders.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.dao.OrderMasterDao
import com.it10x.foodappgstav5_1.printer.pos.POSOrderPrinter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class POSOrdersViewModel(
    private val orderMasterDao: OrderMasterDao,
    private val posPrinter: POSOrderPrinter
) : ViewModel() {

    // 📦 All local orders
    val orders = orderMasterDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 🖨 Re-print existing order
    fun reprintOrder(orderId: String) {
        viewModelScope.launch {
            posPrinter.print(orderId)
        }
    }
}
