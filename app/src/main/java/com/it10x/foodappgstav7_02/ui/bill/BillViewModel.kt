package com.it10x.foodappgstav7_02.ui.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.dao.KotItemDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val tableId: String
) : ViewModel() {

    companion object {
        private const val TAG = "BillViewModel"
    }

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    init {
        Log.d(TAG, "Initializing BillViewModel for tableId: $tableId")
        loadBill()
    }

    private fun loadBill() {
        viewModelScope.launch {
            Log.d(TAG, "Loading bill for tableId: $tableId")
            _uiState.value = BillUiState(loading = true)

            val kotItems = kotItemDao.getItemsForTableSync(tableId)
            Log.d(TAG, "Fetched ${kotItems.size} items from DAO for table $tableId")

            val billingItems = kotItems
                .groupBy { it.productId }
                .map { (productId, group) ->

                    val first = group.first()
                    val quantity = group.sumOf { it.quantity }

                    val subtotal = group.sumOf { it.basePrice * it.quantity }

                    val taxTotal = group.sumOf {
                        if (it.taxType == "exclusive") {
                            it.basePrice * it.quantity * (it.taxRate / 100)
                        } else {
                            0.0
                        }
                    }

                    val finalTotal = subtotal + taxTotal

                    Log.d(
                        TAG,
                        "BillingItem: ${first.name}, quantity: $quantity, subtotal: $subtotal, tax: $taxTotal, total: $finalTotal"
                    )

                    BillingItemUi(
                        id = first.productId,
                        name = first.name,
                        quantity = quantity,
                        subtotal = subtotal,
                        taxTotal = taxTotal,
                        finalTotal = finalTotal
                    )
                }

            val subtotal = billingItems.sumOf { it.subtotal }
            val tax = billingItems.sumOf { it.taxTotal }
            val total = subtotal + tax

            Log.d(TAG, "Bill Summary for table $tableId -> Subtotal: $subtotal, Tax: $tax, Total: $total")

            _uiState.value = BillUiState(
                loading = false,
                items = billingItems,
                subtotal = subtotal,
                tax = tax,
                total = total
            )
        }
    }

    fun payBill(paymentType: String) {
        viewModelScope.launch {
            Log.d(TAG, "Payment done for table $tableId with type: $paymentType. Clearing items...")
            kotItemDao.clearForTable(tableId)   // ✅ cleanup after payment
        }
    }
}
