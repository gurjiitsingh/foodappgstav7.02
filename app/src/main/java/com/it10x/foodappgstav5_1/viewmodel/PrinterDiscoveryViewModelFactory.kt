package com.it10x.foodappgstav5_1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav5_1.printer.discovery.PrinterDiscoveryRepository

class PrinterDiscoveryViewModelFactory(
    private val repo: PrinterDiscoveryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrinterDiscoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrinterDiscoveryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
