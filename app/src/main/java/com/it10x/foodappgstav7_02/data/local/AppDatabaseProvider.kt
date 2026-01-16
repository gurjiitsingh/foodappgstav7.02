package com.it10x.foodappgstav7_02.data.local

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pos.db"     // ⭐ THIS IS THE ONLY DB FILE
            ).fallbackToDestructiveMigration().build().also {
                INSTANCE = it
            }
        }
    }
}






//
//package com.it10x.foodappgstav7_02.data.local
//
//import android.content.Context
//import androidx.room.Room
//import androidx.room.migration.Migration
//import androidx.sqlite.db.ShowSQLiteVersion
//import androidx.sqlite.db.SupportSQLiteDatabase
//
//object AppDatabaseProvider {
//
//    @Volatile
//    private var INSTANCE: AppDatabase? = null
//
//    /**
//     * Migration: 37 → 38
//     * Adds order_sequence table (NO data loss)
//     */
//    private val MIGRATION_37_38 = object : Migration(37, 38) {
//        override fun migrate(db: SupportSQLiteDatabase) {
//
//            db.execSQL(
//                """
//                CREATE TABLE IF NOT EXISTS order_sequence (
//                    id TEXT NOT NULL,
//                    outletId TEXT NOT NULL,
//                    businessDate TEXT NOT NULL,
//                    lastSrNo INTEGER NOT NULL,
//                    updatedAt INTEGER NOT NULL,
//                    PRIMARY KEY(id)
//                )
//                """.trimIndent()
//            )
//
//            // Index for fast lookup & uniqueness
//            db.execSQL(
//                """
//                CREATE UNIQUE INDEX IF NOT EXISTS
//                index_order_sequence_outletId_businessDate
//                ON order_sequence(outletId, businessDate)
//                """.trimIndent()
//            )
//        }
//    }
//
//    fun get(context: Context): AppDatabase {
//        return INSTANCE ?: synchronized(this) {
//            Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                "pos.db"
//            )
//                .addMigrations(MIGRATION_37_38)   // ✅ SAFE
//                // ❌ DO NOT use fallbackToDestructiveMigration anymore
//                .build()
//                .also { INSTANCE = it }
//        }
//    }
//}
