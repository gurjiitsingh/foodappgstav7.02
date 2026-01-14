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
                    parentId = item.parentId,
                    basePrice = item.basePrice,
                    taxRate = item.taxRate,
                    taxType = item.taxType
                )
            }

            val billingItems = grouped.map { (_, items) ->

                val name = items.first().name
                val quantity = items.sumOf { it.quantity }

                val subtotal = items.sumOf {
                    it.basePrice * it.quantity
                }

                val taxTotal = items.sumOf {
                    if (it.taxType == "exclusive")
                        it.basePrice * (it.taxRate / 100) * it.quantity
                    else 0.0
                }

                BillingItemUi(
                    id = items.first().productId, // stable for UI
                    name = name,
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


    // 🔒 Payment logic stays separate (future-safe)
    fun payBill(paymentType: String) {
        // Will be implemented later:
        // - copy DONE KOT → order tables
        // - mark KOT as BILLED
    }
}
