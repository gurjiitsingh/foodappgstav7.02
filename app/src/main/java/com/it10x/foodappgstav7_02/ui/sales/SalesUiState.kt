package com.it10x.foodappgstav7_02.ui.sales

import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
data class SalesUiState(
    val orders: List<PosOrderMasterEntity> = emptyList(),
    val totalSales: Double = 0.0,
    val taxTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val paymentBreakup: Map<String, Double> = emptyMap(),
    val from: Long = 0L,
    val to: Long = 0L,
    val isLoading: Boolean = true
)
