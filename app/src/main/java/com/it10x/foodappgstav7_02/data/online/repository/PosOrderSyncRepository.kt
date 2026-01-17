package com.it10x.foodappgstav7_02.data.online.models.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.it10x.foodappgstav7_02.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_02.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_02.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PosOrderSyncRepository(
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val outletDao: OutletDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Upload ALL locally completed but unsynced orders
     */
    suspend fun syncPendingOrders() = withContext(Dispatchers.IO) {

        val outlet = outletDao.getOutlet()
            ?: throw IllegalStateException("Outlet not configured")

        // 🔹 REQUIRED FIELD (Option 1)
        val ownerId = outlet.ownerId
        val outletId = outlet.outletId

        // 🔹 REQUIRED DAO METHOD (already implied by schema)
        val pendingOrders = orderMasterDao.getPendingSyncOrders()

        if (pendingOrders.isEmpty()) {
            Log.d("ORDER_SYNC", "No pending orders to sync")
            return@withContext
        }

        val batch = firestore.batch()

        pendingOrders.forEach { order ->

            val orderRef = firestore
                .collection("orderMaster")
                .document(order.id)

            val orderItems = orderProductDao.getByOrderIdSync(order.id)

            // -------- ORDER MASTER --------
            batch.set(
                orderRef,
                mapOf(
                    "id" to order.id,
                    "srno" to order.srno,
                    "ownerId" to ownerId,
                    "outletId" to outletId,

                    "orderType" to order.orderType,
                    "tableNo" to order.tableNo,

                    "itemTotal" to order.itemTotal,
                    "taxTotal" to order.taxTotal,
                    "discountTotal" to order.discountTotal,
                    "grandTotal" to order.grandTotal,

                    "paymentType" to order.paymentType,
                    "paymentStatus" to order.paymentStatus,
                    "orderStatus" to order.orderStatus,

                    "source" to "POS",

                    "createdAt" to FieldValue.serverTimestamp(),
                    "syncStatus" to "SYNCED"
                )
            )

            // -------- ORDER ITEMS --------
            orderItems.forEach { item ->
                val itemRef = firestore
                    .collection("orderProducts")
                    .document(item.id)

                batch.set(
                    itemRef,
                    mapOf(
                        "id" to item.id,
                        "orderMasterId" to order.id,
                        "name" to item.name,
                        "quantity" to item.quantity,
                        "basePrice" to item.basePrice,
                        "itemSubtotal" to item.itemSubtotal,
                        "taxRate" to item.taxRate,
                        "taxType" to item.taxType,
                        "taxAmount" to item.taxAmountPerItem,
                        "taxTotal" to item.taxTotal,
                        "finalPrice" to item.finalPricePerItem,
                        "finalTotal" to item.finalTotal,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }

        batch.commit().addOnSuccessListener {
            Log.d("ORDER_SYNC", "Batch sync success (${pendingOrders.size} orders)")
        }.addOnFailureListener { e ->
            Log.e("ORDER_SYNC", "Batch sync failed", e)
            throw e
        }

        // 🔹 MARK LOCAL ORDERS AS SYNCED
        orderMasterDao.markOrdersSynced(
            ids = pendingOrders.map { it.id },
            time = System.currentTimeMillis()
        )
    }
}
