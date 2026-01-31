package com.it10x.foodappgstav7_02.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_02.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
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

            // ✅ Build POSOrdersRepository manually (with all required DAOs)
            val repository = POSOrdersRepository(
                db = db,                            // ✅ Added missing DB reference
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                cartDao = db.cartDao(),
                tableDao = db.tableDao()            // ✅ Added missing tableDao
            )

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository,
                outletDao = db.outletDao(),
                tableId = tableId,
                orderType = orderType,
                repository = repository,              // ✅ Proper repository instance
                printerManager = printerManager,       // ✅ Printer manager instance
                outletRepository = OutletRepository(db.outletDao())
                ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
