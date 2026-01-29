package com.it10x.foodappgstav7_02.ui.bill

data class BillingItemUi(
    val id: String,
    val name: String,
    val basePrice: Double,
    val quantity: Int,
    val finalTotal: Double,
    val itemtotal: Double,
    val taxTotal: Double
)
