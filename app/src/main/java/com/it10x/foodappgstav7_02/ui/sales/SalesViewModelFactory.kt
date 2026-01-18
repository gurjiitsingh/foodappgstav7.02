package com.it10x.foodappgstav7_02.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.pos.dao.SalesMasterDao

class SalesViewModelFactory(
    private val salesMasterDao: SalesMasterDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(
                salesMasterDao = salesMasterDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
