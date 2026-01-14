package com.it10x.foodappgstav7_02.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.it10x.foodappgstav7_02.data.local.dao.*
import com.it10x.foodappgstav7_02.data.local.entities.*
import com.it10x.foodappgstav7_02.data.local.entities.config.*

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        PosOrderMasterEntity::class,
        PosOrderItemEntity::class,
        PosCartEntity::class,
        OutletEntity::class,
        TableEntity::class,
        PosKotItemEntity::class,
        PosKotBatchEntity::class
    ],
    version = 34,              // ⬆️ increment version since schema changed
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderMasterDao(): OrderMasterDao
    abstract fun orderProductDao(): OrderProductDao
    abstract fun outletDao(): OutletDao
    abstract fun cartDao(): CartDao
    abstract fun tableDao(): TableDao
        abstract fun kotBatchDao(): KotBatchDao
    abstract fun kotItemDao(): KotItemDao
}
