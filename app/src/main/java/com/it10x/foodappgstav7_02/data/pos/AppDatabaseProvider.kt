package com.it10x.foodappgstav7_02.data.pos

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



