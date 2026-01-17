package com.it10x.foodappgstav7_02.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.OrderSequenceRepository

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val orderType: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {

            // ✅ DB (Application context is correct here)
            val db = AppDatabaseProvider.get(application)

            // ✅ Repository for atomic SR No
            val orderSequenceRepository = OrderSequenceRepository(db)

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository, // ✅ ADD
                outletDao = db.outletDao(),                         // ✅ ADD
                tableId = tableId,
                orderType = orderType
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
