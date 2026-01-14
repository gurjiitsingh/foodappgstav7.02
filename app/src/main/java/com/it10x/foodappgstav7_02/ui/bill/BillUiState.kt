package com.it10x.foodappgstav7_02.ui.bill

data class BillUiState(
    val loading: Boolean = true,
    val items: List<BillingItemUi> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0
)
