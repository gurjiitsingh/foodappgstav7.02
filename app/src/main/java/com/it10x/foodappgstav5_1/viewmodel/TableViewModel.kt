package com.it10x.foodappgstav5_1.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.AppDatabaseProvider
import com.it10x.foodappgstav5_1.data.local.entities.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

                    val color = when {
                        table.status == "AVAILABLE" -> TableColor.GRAY
                        table.status == "BILL_REQUESTED" -> TableColor.RED
                        openOrders.isNotEmpty() -> TableColor.GREEN
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

//    fun closeTableOnly(tableNo: String) {
//        viewModelScope.launch {
//            repository.closeOrdersForTable(tableNo)
//        }
//    }

    fun closeTable(tableId: String) {
        viewModelScope.launch {
            orderDao.closeTableOrders(tableId, System.currentTimeMillis())
            dao.updateStatus(tableId, "AVAILABLE")
            loadTables()
        }
    }



//    fun markAvailable(tableId: String?) {
//        viewModelScope.launch {
//            dao.insertAll(
//                _tables.value.map {
//                    if (it.id == tableId)
//                        it.copy(status = "AVAILABLE", activeOrderId = null)
//                    else it
//                }
//            )
//            loadTables()
//        }
//    }


}
