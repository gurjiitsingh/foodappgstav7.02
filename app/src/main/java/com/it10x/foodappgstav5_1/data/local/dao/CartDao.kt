package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart WHERE tableId = :tableId ORDER BY createdAt ASC")
    fun getCartForTable(tableId: String?): Flow<List<PosCartEntity>>


    @Query("SELECT * FROM cart WHERE productId = :id AND tableId = :tableId LIMIT 1")
    suspend fun getByIdForTable(id: String, tableId: String): PosCartEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: PosCartEntity)

    @Update
    suspend fun update(product: PosCartEntity)

    @Delete
    suspend fun delete(product: PosCartEntity)

    @Query("DELETE FROM cart WHERE tableId = :tableId")
    suspend fun clearCart(tableId: String)


    @Query("""
    SELECT * FROM cart 
    WHERE tableId = :tableId 
    AND sentToKitchen = 0
    ORDER BY createdAt ASC
""")
    fun getUnsentItems(tableId: String?): Flow<List<PosCartEntity>>

    @Query("""
    UPDATE cart 
    SET sentToKitchen = 1 
    WHERE tableId = :tableId
""")
    suspend fun markAllSent(tableId: String)


}
