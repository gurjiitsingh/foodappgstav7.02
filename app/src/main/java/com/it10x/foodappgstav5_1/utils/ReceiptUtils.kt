package com.it10x.foodappgstav5_1.utils

fun formatAmount(value: Double?): String {
    return "%.2f".format(value ?: 0.0)
}
