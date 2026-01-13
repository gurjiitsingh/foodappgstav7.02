package com.it10x.foodappgstav5_1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav5_1.printer.PrinterManager

class OrdersViewModelFactory(
    private val printerManager: PrinterManager // only inject dependencies
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrdersViewModel(printerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

