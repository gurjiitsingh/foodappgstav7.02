package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity


@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: PosOrderMasterEntity): Long

    @Insert
    suspend fun insertItems(items: List<PosOrderItemEntity>)

    @Query("SELECT * FROM pos_order_master ORDER BY createdAt DESC")
    suspend fun getOrders(): List<PosOrderMasterEntity>
}
