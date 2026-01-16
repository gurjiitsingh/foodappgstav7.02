package com.it10x.foodappgstav7_02.data.local.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.local.AppDatabase
import com.it10x.foodappgstav7_02.data.local.dao.CartDao
import com.it10x.foodappgstav7_02.data.local.dao.OrderMasterDao
import com.it10x.foodappgstav7_02.data.local.dao.OrderProductDao
import com.it10x.foodappgstav7_02.data.local.dao.TableDao
import com.it10x.foodappgstav7_02.data.local.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager

class POSOrdersViewModelFactory(
    private val db: AppDatabase,
    private val printerManager: PrinterManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(POSOrdersViewModel::class.java)) {

            val repository = POSOrdersRepository(
                db = db,                                // ✅ ADD THIS
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                cartDao = db.cartDao(),
                tableDao = db.tableDao()
            )

            @Suppress("UNCHECKED_CAST")
            return POSOrdersViewModel(repository, printerManager) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

