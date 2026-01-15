package com.it10x.foodappgstav7_02.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.local.entities.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object TableStatus {
    const val AVAILABLE = "AVAILABLE"
    const val OCCUPIED = "OCCUPIED"
    const val BILL_REQUESTED = "BILL_REQUESTED"
}
class TableViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabaseProvider.get(app).tableDao()

    private val _tables = MutableStateFlow<List<TableUiState>>(emptyList())
    val tables: StateFlow<List<TableUiState>> = _tables

    private val orderDao = AppDatabaseProvider.get(app).orderMasterDao()

    data class TableUiState(
        val table: TableEntity,
        val runningAmount: Double,
        val color: TableColor
    )

    enum class TableColor {
        GREEN,   // 🟢 running
        YELLOW,  // 🟡 bill requested
        RED,     // 🔴 ready to bill
        GRAY     // ⚪ available
    }

    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = dao.getAll()

                val uiList = tableList.map { table ->
                    val total = orderDao.getRunningTotalForTable(table.id)
                    val openOrders = orderDao.getOpenOrdersForTable(table.id)

//                    val color = when {
//                        table.status == "AVAILABLE" -> TableColor.GRAY
//                        table.status == "BILL_REQUESTED" -> TableColor.RED
//                        openOrders.isNotEmpty() -> TableColor.GREEN
//                        else -> TableColor.GRAY
//                    }

                    val color = when (table.status) {
                        TableStatus.AVAILABLE -> TableColor.GRAY
                        TableStatus.OCCUPIED -> TableColor.GREEN
                        TableStatus.BILL_REQUESTED -> TableColor.RED
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        runningAmount = total,
                        color = color
                    )
                }

                _tables.value = uiList

            } catch (e: Exception) {
                _tables.value = emptyList()
            }
        }
    }


    fun updateStatus(tableId: String, newStatus: String) {
        viewModelScope.launch {
            dao.updateStatus(tableId, newStatus) // ✅ persist
            loadTables()                         // ✅ refresh UI
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
            dao.updateStatus(tableId, "BILL_REQUESTED")
            loadTables()
        }
    }


    fun closeTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.updateStatus(tableId, "AVAILABLE")
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


}
