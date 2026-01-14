package com.it10x.foodappgstav7_02.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_02.data.local.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KotItemDao {


    // -------------------------
    // INSERT
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PosKotItemEntity>)


    // -------------------------
    // FETCH (LIVE BILL)
    // -------------------------
    @Query("""
        SELECT * FROM pos_kot_items
        WHERE tableNo = :tableNo
        ORDER BY createdAt ASC
    """)
    fun getItemsForTable(tableNo: String): Flow<List<PosKotItemEntity>>

    // -------------------------
    // FETCH (FINAL BILL)
    // -------------------------
    @Query("""
        SELECT * FROM pos_kot_items
        WHERE tableNo = :tableNo
    """)
    suspend fun getItemsForTableSync(tableNo: String?): List<PosKotItemEntity>

    // -------------------------
    // CLEANUP AFTER PAYMENT
    // -------------------------
    @Query("""
        DELETE FROM pos_kot_items
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(tableNo: String)


    @Query("""
    UPDATE pos_kot_items
    SET status = :status
    WHERE id = :itemId
""")
    suspend fun updateStatus(
        itemId: String,
        status: String
    )


    @Query("SELECT * FROM pos_kot_items WHERE id = :itemId LIMIT 1")
    suspend fun getItemByIdSync(itemId: String): PosKotItemEntity?
    @Query("""
    SELECT * FROM pos_kot_items
    WHERE tableNo = :tableNo
      AND status = 'PENDING'
    ORDER BY createdAt ASC
""")
    fun getPendingItemsForTable(tableNo: String): Flow<List<PosKotItemEntity>>


    @Query("SELECT * FROM pos_kot_items ORDER BY createdAt ASC")
    fun getAllKotItems(): Flow<List<PosKotItemEntity>>

    @Query("""
    UPDATE pos_kot_items
    SET quantity = :quantity
    WHERE id = :itemId
""")
    suspend fun updateQuantity(itemId: String, quantity: Int)
}
