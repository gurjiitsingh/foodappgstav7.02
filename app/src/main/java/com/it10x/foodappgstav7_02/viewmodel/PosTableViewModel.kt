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

    const val OCCUPIED = "OCCUPIED"        // guests seated (universal)
    // ⚪ No activity
    const val AVAILABLE = "AVAILABLE"

    // 🔵 Items only in cart (no KOT yet)
    const val ORDERING = "ORDERING"

    // 🟡 Items sent to kitchen but NOT printed
    const val KITCHEN = "KITCHEN"

    // 🟢 KOT printed (running order)
    const val KITCHEN_PRINTED = "KITCHEN_PRINTED"

    // 🟣 Items reached bill (bill screen has items)
    const val BILL = "BILL"

    // 🔴 (optional / future)
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
    data class TableUiState(
        val table: TableEntity,
        val runningAmount: Double,
        val color: TableColor,

        val cartCount: Int = 0,
        val kitchenPendingCount: Int = 0,
        val billAmount: Double = 0.0,
        val isBilled: Boolean = false
    )

    enum class TableColor {
        GRAY,     // AVAILABLE
        BLUE,     // ORDERING (cart only)
        GREEN,    // KITCHEN
        RED,       // BILL

    //    YELLOW
    }

    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = dao.getAll()

                val uiList = tableList.map { table ->

                    val total = orderDao.getRunningTotalForTable(table.id)
                    val openOrders = orderDao.getOpenOrdersForTable(table.id)

                    // 🛒 CART COUNT (REAL DATA)
                    val cartCount = cartRepository.getCartCountForTable(table.id)

                    val color = when (table.status) {
                        TableStatus.AVAILABLE -> TableColor.GRAY
                        TableStatus.ORDERING -> TableColor.BLUE
                        TableStatus.OCCUPIED -> TableColor.GREEN
                        TableStatus.BILL_REQUESTED -> TableColor.RED
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        runningAmount = total,
                        color = color,

                        // ✅ THIS IS WHAT UI READS
                        cartCount = cartCount
                    )
                }


                _tables.value = uiList

            } catch (e: Exception) {
                _tables.value = emptyList()
            }
        }
    }


    fun markOrdering(tableId: String) {

        Log.d(
            "CART_DEBUG",
            "In PosTableViewModel:markOrdering:  tableId=${tableId} "
        )
        viewModelScope.launch {

            // ⛔ do not downgrade active states
            val table = dao.getById(tableId) ?: return@launch

            if (
                table.status == TableStatus.KITCHEN ||
                table.status == TableStatus.BILL
            ) {
                return@launch
            }
            Log.d(
                "CART_DEBUG",
                "PosTableViewModel:updateStatus:ORDERING  tableId=${tableId} "
            )
            dao.updateStatus(tableId, TableStatus.ORDERING)
            loadTables()



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

    fun releaseIfOrderingAndCartEmpty(tableNo: String) {
        viewModelScope.launch {
            val table = dao.getById(tableNo) ?: return@launch
            if (table.status != TableStatus.ORDERING) return@launch

            dao.updateStatus(tableNo, TableStatus.AVAILABLE)
            loadTables()
        }
    }


}
