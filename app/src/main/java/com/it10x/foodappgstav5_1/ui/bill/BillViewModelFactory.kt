package com.it10x.foodappgstav5_1.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav5_1.data.local.repository.POSOrdersRepository

class BillViewModelFactory(
    private val repository: POSOrdersRepository,
    private val tableId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            return BillViewModel(repository, tableId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
