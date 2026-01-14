package com.it10x.foodappgstav7_02.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.dao.KotItemDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// 🔹 Billing grouping key (file-level)
private data class BillGroupKey(
    val productId: String,
    val parentId: String?,
    val basePrice: Double,
    val taxRate: Double,
    val taxType: String
)
class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val tableId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState.asStateFlow()

    fun loadBill() {
        viewModelScope.launch {
            _uiState.value = BillUiState(loading = true)

            val kotItems = kotItemDao
                .getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }

            val grouped = kotItems.groupBy { item ->
                BillGroupKey(
                    productId = item.productId,
                   basePrice = item.basePrice,
                    taxRate = item.taxRate,
                    taxType = item.taxType,
                   parentId = item.parentId
                )
            }

            val billingItems = grouped.map { (_, items) ->

                val quantity = items.sumOf { it.quantity }
                val basePrice = items.first().basePrice
                val taxRate = items.first().taxRate
                val taxType = items.first().taxType

                val subtotal = basePrice * quantity

                val taxTotal =
                    if (taxType == "exclusive")
                        basePrice * (taxRate / 100) * quantity
                    else 0.0

                BillingItemUi(
                    id = items.first().productId, // stable UI id
                    name = items.first().name,
                    quantity = quantity,
                    subtotal = subtotal,
                    taxTotal = taxTotal,
                    finalTotal = subtotal + taxTotal
                )
            }

            val subtotal = billingItems.sumOf { it.subtotal }
            val tax = billingItems.sumOf { it.taxTotal }

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
