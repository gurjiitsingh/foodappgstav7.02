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




    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()

    data class TableUiState(
        val table: TableEntity,
        val color: TableColor,
        val isBilled: Boolean = false
    ) {
        val cartCount get() = table.cartCount
        val kitchenPendingCount get() = table.kitchenCount
        val billDoneCount get() = table.billCount
        val billAmount get() = table.billAmount
        val runningAmount get() = table.billAmount
    }

    enum class TableColor {
        GRAY,
        BLUE,
        GREEN,
        RED
    }

    private fun observeTables() {
        viewModelScope.launch {
            dao.observeAllTables().collect { tableList ->

                val uiList = tableList.map { table ->

                    val isBilled = table.billCount > 0 || table.kitchenCount > 0

                    val color = when {
                        table.billCount > 0 -> TableColor.RED
                        table.kitchenCount > 0 -> TableColor.GREEN
                        table.cartCount > 0 -> TableColor.BLUE
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        color = color,
                        isBilled = isBilled
                    )
                }

                _tables.value = uiList
            }
        }
    }

    init {
        observeTables()
    }

    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = dao.getAll()

                // 🔹 Add this to print all tables and their area values
//                tableList.forEach { table ->
//                    Log.d("TABLE_DEBUG", "Table ${table.id} (${table.tableName}) → area=${table.area}")
//                }

                val uiList = tableList.map { table ->

                    val isBilled = table.billCount > 0 || table.kitchenCount > 0

                    val color = when {
                        table.billCount > 0 -> TableColor.RED
                        table.kitchenCount > 0 -> TableColor.GREEN
                        table.cartCount > 0 -> TableColor.BLUE
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        color = color,
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
          //  loadTables()
        }
    }

    fun updateStatus(tableId: String, newStatus: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, newStatus)
          //  loadTables()
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
           // loadTables()
        }
    }

    fun closeTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.updateStatus(tableId, TableStatus.AVAILABLE)
           // loadTables()
        }
    }

    fun occupyTable(tableId: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, TableStatus.OCCUPIED)
         //   loadTables()
        }
    }

    fun releaseTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.clearActiveOrder(tableId)
            dao.updateStatus(tableId, TableStatus.AVAILABLE)
           // loadTables()
        }
    }

    fun releaseIfOrderingAndCartEmpty(tableNo: String) {
        viewModelScope.launch {
            val table = dao.getById(tableNo) ?: return@launch
            if (table.status != TableStatus.ORDERING) return@launch
            dao.updateStatus(tableNo, TableStatus.AVAILABLE)
           // loadTables()
        }
    }

    // ============================
    // 🔹 NEW CODE ADDED (SAFE)
    // ============================
    // Use ONLY when you need raw counts for a single table

}
