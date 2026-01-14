package com.it10x.foodappgstav7_02.ui.orders.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.local.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.data.local.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_02.printer.PrinterManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_02.printer.PosReceiptBuilder
class LocalOrderDetailViewModel(
    private val orderId: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _orderInfo = MutableStateFlow<PosOrderMasterEntity?>(null)
    val orderInfo: StateFlow<PosOrderMasterEntity?> = _orderInfo

    val products: StateFlow<List<PosOrderItemEntity>> =
        flow { emit(repository.getOrderItems(orderId)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subtotal = products.map { it.sumOf { p -> p.itemSubtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val taxTotal = products.map { it.sumOf { p -> p.taxTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val grandTotal = products.map { it.sumOf { p -> p.finalTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    init {
        viewModelScope.launch {
            _orderInfo.value = repository.getOrderById(orderId)
        }
    }
}
