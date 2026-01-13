package com.it10x.foodappgstav5_1.ui.bill
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.repository.POSOrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BillUiState(
    val loading: Boolean = true,
    val items: List<PosOrderItemEntity> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val tableUpdated: Boolean = false
)

class BillViewModel(
    private val repository: POSOrdersRepository,
    private val tableId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState.asStateFlow()

    fun loadBill() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)  // mark loading
            val orderItems = repository.getAllItemsForTable(tableId)
            repository.markTableBillRequested(tableId)

            val subtotal = orderItems.sumOf { it.itemSubtotal }
            val tax = orderItems.sumOf { it.taxTotal }
            val total = subtotal + tax

            _uiState.value = BillUiState(
                loading = false,                 // done loading
                items = orderItems,
                subtotal = subtotal,
                tax = tax,
                total = total,
                tableUpdated = true
            )
        }
    }

    fun payBill(paymentType: String) {
        viewModelScope.launch {
            repository.markOrdersPaid(tableId, paymentType)
            _uiState.value = _uiState.value.copy(tableUpdated = true)
        }
    }
}
