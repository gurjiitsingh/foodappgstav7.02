// File: SalesViewModel.kt
package com.it10x.foodappgstav7_02.ui.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.pos.dao.SalesMasterDao
import com.it10x.foodappgstav7_02.data.pos.dao.PaymentBreakup
import com.it10x.foodappgstav7_02.data.pos.dao.TaxDiscountSummary
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(
    private val salesMasterDao: SalesMasterDao
) : ViewModel() {

    companion object {
        private const val TAG = "SALESDEBUG"
    }

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _fromDate = MutableStateFlow(startOfToday())
    private val _toDate = MutableStateFlow(endOfToday())

    init {
        refreshSales()
    }

    fun setDateRange(from: Long, to: Long) {
        _fromDate.value = from
        _toDate.value = to
        refreshSales()
    }

//    fun refreshSales() {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            val from = _fromDate.value
//            val to = _toDate.value
//
//            Log.i(TAG, "refreshSales() called with from=$from, to=$to")
//
//            val salesFlow: Flow<List<PosOrderMasterEntity>> = salesMasterDao.getSales(from, to)
//            val sales = salesFlow.first()
//
//            Log.i(TAG, "Filtered sales count=${sales.size}")
//
//            val total = salesMasterDao.getTotalSales(from, to)
//            val payment = salesMasterDao.getPaymentBreakup(from, to)
//            val taxDiscount = salesMasterDao.getTaxDiscountSummary(from, to)
//
//            _uiState.value = SalesUiState(
//                isLoading = false,
//                orders = sales,
//                totalSales = total,
//                taxTotal = taxDiscount.taxTotal,
//                discountTotal = taxDiscount.discountTotal,
//                paymentBreakup = payment.associate { it.paymentType to it.total },
//                from = from,
//                to = to
//            )
//
//            Log.i(TAG, "Total sales=$total, Tax=${taxDiscount.taxTotal}, Discount=${taxDiscount.discountTotal}")
//        }
//    }


    fun refreshSales() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // ==========================
            // TEST MODE: IGNORE DATE RANGE
            // ==========================
            Log.i(TAG, "refreshSales() called - TEST MODE: ignoring time range")

            val salesFlow: Flow<List<PosOrderMasterEntity>> = salesMasterDao.getAllPaidOrders() // fetch all paid
            val sales = salesFlow.first()

            Log.i(TAG, "All PAID orders count=${sales.size}")

            val total = sales.sumOf { it.grandTotal }               // simple total
            val paymentBreakup = sales.groupBy { it.paymentType }
                .mapValues { it.value.sumOf { o -> o.grandTotal } }
            val taxTotal = sales.sumOf { it.taxTotal }
            val discountTotal = sales.sumOf { it.discountTotal }

            _uiState.value = SalesUiState(
                isLoading = false,
                orders = sales,
                totalSales = total,
                taxTotal = taxTotal,
                discountTotal = discountTotal,
                paymentBreakup = paymentBreakup
            )

            Log.i(TAG, "Total sales=$total, Tax=$taxTotal, Discount=$discountTotal")
        }
    }



    // ---------------- HELPER FUNCTIONS ----------------


    fun startOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun endOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
//    fun startOfToday(): Long {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        return calendar.timeInMillis
//    }
//
//    fun endOfToday(): Long {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 23)
//        calendar.set(Calendar.MINUTE, 59)
//        calendar.set(Calendar.SECOND, 59)
//        calendar.set(Calendar.MILLISECOND, 999)
//        return calendar.timeInMillis
//    }

    fun formatDate(ts: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(ts))
    }

    fun startOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun endOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
