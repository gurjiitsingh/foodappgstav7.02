package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()




}
