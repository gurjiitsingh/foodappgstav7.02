package com.it10x.foodappgstav5_1.data.local.dao

import androidx.room.*
import com.it10x.foodappgstav5_1.data.local.entities.CategoryEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clear()
}




//package com.it10x.foodappgstav5_1.data.local.dao
//
//import androidx.room.*
//import com.it10x.foodappgstav5_1.data.local.entities.CategoryEntity
//import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface CategoryDao {
//
//    @Query("SELECT * FROM categories ORDER BY name")
//    fun getAll(): Flow<List<CategoryEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(list: List<CategoryEntity>)
//
//    @Query("DELETE FROM categories")
//    suspend fun clear()
//
//
//    //extra
//
//    @Query("SELECT * FROM cart")
//    fun getCart(): Flow<List<PosCartEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(item: PosCartEntity)
//
//    @Query("DELETE FROM cart")
//    suspend fun clearCart()
//
//    @Query("SELECT * FROM cart WHERE productId = :id LIMIT 1")
//    suspend fun getById(id: String): PosCartEntity?
//
//    @Update
//    suspend fun update(item: PosCartEntity)
//
//    @Delete
//    suspend fun delete(item: PosCartEntity)
//
//
//
//
//}
