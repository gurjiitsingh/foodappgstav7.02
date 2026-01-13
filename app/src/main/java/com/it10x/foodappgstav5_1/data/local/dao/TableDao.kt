package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav5_1.data.local.entities.TableEntity

@Dao
interface TableDao {

    @Query("DELETE FROM tables")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<TableEntity>)

    @Query("SELECT * FROM tables ORDER BY id ASC")
    suspend fun getAll(): List<TableEntity>

    @Query("UPDATE tables SET status = :status WHERE id = :tableId")
    suspend fun updateStatus(tableId: String, status: String)

    // ✅ when order starts
    @Query("""
        UPDATE tables
        SET activeOrderId = :orderId,
            status = 'OCCUPIED'
        WHERE id = :tableId
    """)
    suspend fun setActiveOrder(tableId: String, orderId: String)

    // ✅ when table is closed
    @Query("""
        UPDATE tables
        SET activeOrderId = NULL,
            status = 'AVAILABLE'
        WHERE id = :tableId
    """)
    suspend fun clearActiveOrder(tableId: String)
}

