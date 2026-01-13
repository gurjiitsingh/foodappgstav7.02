package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav5_1.data.local.entities.PosKotItemEntity
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
    suspend fun getItemsForTableSync(tableNo: String): List<PosKotItemEntity>

    // -------------------------
    // CLEANUP AFTER PAYMENT
    // -------------------------
    @Query("""
        DELETE FROM pos_kot_items
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(tableNo: String)
}
