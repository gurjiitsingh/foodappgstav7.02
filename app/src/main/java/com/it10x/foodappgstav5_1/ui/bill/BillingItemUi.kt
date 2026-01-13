package com.it10x.foodappgstav5_1.ui.bill

data class BillingItemUi(
    val id: String,
    val name: String,
    val quantity: Int,
    val finalTotal: Double,
    val subtotal: Double,
    val taxTotal: Double
)
