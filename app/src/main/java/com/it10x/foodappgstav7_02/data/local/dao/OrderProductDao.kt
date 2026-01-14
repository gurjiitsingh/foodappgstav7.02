package com.it10x.foodappgstav7_02.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav7_02.data.local.entities.PosOrderItemEntity

import kotlinx.coroutines.flow.Flow



@Dao
interface OrderProductDao {

    // -------------------------
    // INSERT
    // -------------------------
    @Insert
    suspend fun insertAll(items: List<PosOrderItemEntity>)

    // -------------------------
    // ORDER DETAILS (single order)
    // -------------------------
    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    fun getByOrderId(orderId: String): Flow<List<PosOrderItemEntity>>

    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    suspend fun getByOrderIdSync(orderId: String): List<PosOrderItemEntity>

    // -------------------------
    // 🔥 FINAL BILL (ALL OPEN ORDERS OF A TABLE)
    // -------------------------
    @Query("""
        SELECT i.*
        FROM pos_order_items i
        INNER JOIN pos_order_master o
            ON i.orderMasterId = o.id
        WHERE o.tableNo = :tableNo
          AND o.orderStatus IN ('NEW', 'OPEN')
        ORDER BY o.createdAt ASC, i.createdAt ASC
    """)
    suspend fun getAllItemsForTable(tableNo: String): List<PosOrderItemEntity>


    @Query("""
    SELECT i.* FROM pos_order_items i
    INNER JOIN pos_order_master m
        ON i.orderMasterId = m.id
    WHERE m.tableNo = :tableNo
      AND m.orderStatus IN ('NEW', 'OPEN')
""")
    suspend fun getItemsForTable(tableNo: String): List<PosOrderItemEntity>



}



