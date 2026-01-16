package com.it10x.foodappgstav7_02.data.local.repository

import com.it10x.foodappgstav7_02.data.local.dao.CartDao
import com.it10x.foodappgstav7_02.data.local.dao.OrderMasterDao
import com.it10x.foodappgstav7_02.data.local.dao.OrderProductDao
import com.it10x.foodappgstav7_02.data.local.dao.TableDao
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import androidx.room.withTransaction
import com.it10x.foodappgstav7_02.data.local.AppDatabase
import com.it10x.foodappgstav7_02.data.local.repository.OrderSequenceRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class POSOrdersRepository(
    private val db: AppDatabase, // 🔹 ADD THIS
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
    // CART (per table)
    // -------------------------
    fun getCartItems(tableId: String?, orderType: String): Flow<List<PosCartEntity>> {
        return if (orderType == "DINE_IN" && tableId != null) {
            cartDao.getCartForTable(tableId)
        } else {
            // Takeaway or Delivery uses sessionId pattern
            val prefix = "${orderType}-"
            cartDao.getCartForSessionPrefix(prefix)
        }
    }



//    fun getUnsentItems(tableId: String?): Flow<List<PosCartEntity>> =
//        cartDao.getUnsentItems(tableId)

    fun getUnsentItems(tableId: String): Flow<List<PosCartEntity>> =
        cartDao.getUnsentItems(tableId)
    suspend fun markAllSent(tableId: String) {
        cartDao.markAllSent(tableId)
    }

//    suspend fun clearCart(tableId: String) {
//        cartDao.clearCart(tableId)
//    }

    suspend fun clearCart(orderType: String, tableId: String?) {
        when (orderType) {
            "DINE_IN" -> {
                if (!tableId.isNullOrBlank()) {
                    cartDao.clearCart(tableId)      // tableId used as sessionId
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
    // TABLE STATE
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
        tableDao.setActiveOrder(tableId, "") // or create a clearActiveOrder DAO
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





    // -------------------------
    // INSERT ORDER
    // -------------------------
//    suspend fun insertOrder(
//        orderMaster: PosOrderMasterEntity,
//        cartItems: List<PosCartEntity>
//    ) {
//        // 1️⃣ Insert order master
//        orderMasterDao.insert(orderMaster)
//
//        // 2️⃣ Mark table RUNNING
//        orderMaster.tableNo?.let { tableId ->
//            markTableRunning(tableId, orderMaster.id)
//        }
//
//        val now = System.currentTimeMillis()
//
//        // 3️⃣ Cart → Order items
//        val orderItems = cartItems.map { cart ->
//            val itemSubtotal = cart.basePrice * cart.quantity
//            val taxAmount =
//                if (cart.taxType == "exclusive") cart.basePrice * (cart.taxRate / 100) else 0.0
//
//            val finalPrice = cart.basePrice + taxAmount
//
//            PosOrderItemEntity(
//                id = UUID.randomUUID().toString(),
//                orderMasterId = orderMaster.id,
//                productId = cart.productId,
//                name = cart.name,
//                categoryId = cart.categoryId,
//                parentId = cart.parentId,
//                isVariant = cart.parentId != null,
//                basePrice = cart.basePrice,
//                quantity = cart.quantity,
//                itemSubtotal = itemSubtotal,
//                taxRate = cart.taxRate,
//                taxType = cart.taxType,
//                taxAmountPerItem = taxAmount,
//                taxTotal = taxAmount * cart.quantity,
//                finalPricePerItem = finalPrice,
//                finalTotal = finalPrice * cart.quantity,
//                source = "POS",
//                createdAt = now
//            )
//        }
//
//        // 4️⃣ Insert items
//        orderProductDao.insertAll(orderItems)
//
//        // 5️⃣ Clear cart for that table
//       // orderMaster.tableNo?.let { clearCart(it) }
//        orderMaster.tableNo?.let { markAllSent(it) }
//    }
}
