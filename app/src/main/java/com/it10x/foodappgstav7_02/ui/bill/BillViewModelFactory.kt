package com.it10x.foodappgstav7_02.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val orderType: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {

            // ✅ Database (context from Application is safe)
            val db = AppDatabaseProvider.get(application)

            // ✅ Repository for atomic SR No generation
            val orderSequenceRepository = OrderSequenceRepository(db)

            // ✅ PrinterManager instance (required by BillViewModel)
            val printerManager = PrinterManager(application.applicationContext)

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository,
                outletDao = db.outletDao(),
                tableId = tableId,
                orderType = orderType,
                printerManager = printerManager // ✅ Added here
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
