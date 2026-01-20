package com.it10x.foodappgstav7_02.data.pos.repository

import com.it10x.foodappgstav7_02.data.pos.AppDatabase
import com.it10x.foodappgstav7_02.data.pos.dao.CartDao
import com.it10x.foodappgstav7_02.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_02.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_02.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_02.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_02.data.pos.dao.TableDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class POSOrdersRepository(
    private val db: AppDatabase, // 🔹 Keep DB reference for KOT or outlet lookups
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val cartDao: CartDao,
    private val tableDao: TableDao
) {

    // -------------------------
    // ORDER DETAILS
    // -------------------------
    suspend fun getOrderById(orderId: String): PosOrderMasterEntity? {
        return orderMasterDao.getOrderById(orderId)
    }

    // 🔹 NEW: API-24 safe business date
    private fun businessDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
    }

    // -------------------------
    // CART (per table/session)
    // -------------------------
    fun getCartItems(tableId: String?, orderType: String): Flow<List<PosCartEntity>> {
        return if (orderType == "DINE_IN" && tableId != null) {
            cartDao.getCartForTable(tableId)
        } else {
            // Takeaway or Delivery uses sessionId prefix
            val prefix = "${orderType}-"
            cartDao.getCartForSessionPrefix(prefix)
        }
    }

    fun getUnsentItems(tableId: String): Flow<List<PosCartEntity>> =
        cartDao.getUnsentItems(tableId)

    suspend fun markAllSent(tableId: String) {
        cartDao.markAllSent(tableId)
    }

    // ✅ Clears cart safely depending on order type
    suspend fun clearCart(orderType: String, tableId: String?) {
        when (orderType) {
            "DINE_IN" -> {
                if (!tableId.isNullOrBlank()) {
                    cartDao.clearCart(tableId)      // Table-based session
                }
            }
            "TAKEAWAY", "DELIVERY" -> {
                cartDao.clearCartByPrefix("$orderType-")
            }
            else -> {
                // fallback just in case
                cartDao.clearCartByPrefix("$orderType-")
            }
        }
    }

    // -------------------------
    // TABLE STATE MANAGEMENT
    // -------------------------
    suspend fun markTableRunning(tableId: String, orderId: String) {
        tableDao.updateStatus(tableId, "OCCUPIED")
        tableDao.setActiveOrder(tableId, orderId)
    }

    suspend fun markTableBillRequested(tableId: String) {
        tableDao.updateStatus(tableId, "BILL_REQUESTED")
    }

    suspend fun closeTable(tableId: String) {
        tableDao.updateStatus(tableId, "AVAILABLE")
        tableDao.setActiveOrder(tableId, "") // or a clearActiveOrder() DAO
    }

    // -------------------------
    // ORDERS
    // -------------------------
    suspend fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity> {
        return orderMasterDao.getPagedOrders(limit, offset)
    }

    suspend fun getOpenOrdersForTable(tableNo: String): List<PosOrderMasterEntity> {
        return orderMasterDao.getOpenOrdersForTable(tableNo)
    }

    suspend fun getOrderItems(orderId: String): List<PosOrderItemEntity> {
        return orderProductDao.getByOrderIdSync(orderId)
    }

    suspend fun getAllItemsForTable(tableNo: String): List<PosOrderItemEntity> {
        return orderProductDao.getAllItemsForTable(tableNo)
    }

    // -------------------------
    // BILLING / PAYMENT
    // -------------------------
    suspend fun markOrdersPaid(
        tableNo: String,
        paymentType: String
    ) {
        orderMasterDao.closeTableOrders(
            tableId = tableNo,
            time = System.currentTimeMillis()
        )
    }

    // (Optional) Future: Add KOT management helper functions here if needed
    // e.g. fetch pending KOT items, clear printed KOTs, etc.
}
