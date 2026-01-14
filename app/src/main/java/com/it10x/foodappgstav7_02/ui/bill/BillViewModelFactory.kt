package com.it10x.foodappgstav7_02.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.local.AppDatabaseProvider

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            val db = AppDatabaseProvider.get(application)
            return BillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                tableId = tableId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
