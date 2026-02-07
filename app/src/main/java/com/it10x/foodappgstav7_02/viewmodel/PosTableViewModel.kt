package com.it10x.foodappgstav7_02.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.entities.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object TableStatus {

    const val OCCUPIED = "OCCUPIED"
    const val AVAILABLE = "AVAILABLE"
    const val ORDERING = "ORDERING"
    const val KITCHEN = "KITCHEN"
    const val KITCHEN_PRINTED = "KITCHEN_PRINTED"
    const val BILL = "BILL"
    const val BILL_REQUESTED = "BILL_REQUESTED"
}

class PosTableViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabaseProvider.get(app).tableDao()

    private val _tables = MutableStateFlow<List<TableUiState>>(emptyList())
    val tables: StateFlow<List<TableUiState>> = _tables

    private val orderDao = AppDatabaseProvider.get(app).orderMasterDao()

    private val cartRepository =
        com.it10x.foodappgstav7_02.data.pos.repository.CartRepository(
            AppDatabaseProvider.get(app).cartDao()
        )

    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()

    data class TableUiState(
        val table: TableEntity,
        val runningAmount: Double,
        val color: TableColor,
        val cartCount: Int = 0,
        val kitchenPendingCount: Int = 0,
        val billDoneCount: Int = 0,
        val billAmount: Double = 0.0,
        val isBilled: Boolean = false
    )

    enum class TableColor {
        GRAY,
        BLUE,
        GREEN,
        RED
    }

    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = dao.getAll()

                // 🔹 Add this to print all tables and their area values
                tableList.forEach { table ->
                    Log.d("TABLE_DEBUG", "Table ${table.id} (${table.tableName}) → area=${table.area}")
                }

                val uiList = tableList.map { table ->

                    val cartCount = cartRepository.getCartCountForTable(table.id)
                    val kitchenPendingCount = kotItemDao.countKitchenPending(table.id)
                    val billDoneCount = kotItemDao.countBillDone(table.id)
                    val billAmount = kotItemDao.billAmountForTable(table.id)

                    val isBilled = billDoneCount > 0 || kitchenPendingCount > 0

                    val color = when {
                        billDoneCount > 0 -> TableColor.RED
                        kitchenPendingCount > 0 -> TableColor.GREEN
                        cartCount > 0 -> TableColor.BLUE
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        runningAmount = billAmount,
                        color = color,
                        cartCount = cartCount,
                        billDoneCount = billDoneCount,
                        kitchenPendingCount = kitchenPendingCount,
                        billAmount = billAmount,
                        isBilled = isBilled
                    )
                }

                _tables.value = uiList

            } catch (e: Exception) {
                _tables.value = emptyList()
            }
        }
    }

    fun markOrdering(tableId: String) {
        Log.d("CART_DEBUG", "markOrdering tableId=$tableId")
        viewModelScope.launch {
            val table = dao.getById(tableId) ?: return@launch
            if (table.status == TableStatus.KITCHEN || table.status == TableStatus.BILL) return@launch
            dao.updateStatus(tableId, TableStatus.ORDERING)
            loadTables()
        }
    }

    fun updateStatus(tableId: String, newStatus: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, newStatus)
            loadTables()
        }
    }

    fun markRunning(tableId: String, orderId: String) {
        viewModelScope.launch {
            dao.setActiveOrder(tableId, orderId)
            loadTables()
        }
    }

    fun requestBill(tableId: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, TableStatus.BILL_REQUESTED)
            loadTables()
        }
    }

    fun closeTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.updateStatus(tableId, TableStatus.AVAILABLE)
            loadTables()
        }
    }

    fun occupyTable(tableId: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, TableStatus.OCCUPIED)
            loadTables()
        }
    }

    fun releaseTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.clearActiveOrder(tableId)
            dao.updateStatus(tableId, TableStatus.AVAILABLE)
            loadTables()
        }
    }

    fun releaseIfOrderingAndCartEmpty(tableNo: String) {
        viewModelScope.launch {
            val table = dao.getById(tableNo) ?: return@launch
            if (table.status != TableStatus.ORDERING) return@launch
            dao.updateStatus(tableNo, TableStatus.AVAILABLE)
            loadTables()
        }
    }

    // ============================
    // 🔹 NEW CODE ADDED (SAFE)
    // ============================
    // Use ONLY when you need raw counts for a single table
    suspend fun getCountsForTable(tableId: String): Triple<Int, Int, Double> {
        val cartCount = cartRepository.getCartCountForTable(tableId)
        val kitchenPending = kotItemDao.countKitchenPending(tableId)
        val billAmount = kotItemDao.billAmountForTable(tableId)

        Log.d(
            "TABLE_COUNT_DEBUG",
            "table=$tableId cart=$cartCount kitchen=$kitchenPending bill=$billAmount"
        )

        return Triple(cartCount, kitchenPending, billAmount)
    }
}
