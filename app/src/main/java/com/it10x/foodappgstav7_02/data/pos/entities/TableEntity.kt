package com.it10x.foodappgstav7_02.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: String,
    val tableName: String,
    val status: String,  // AVAILABLE / OCCUPIED / BILL_REQUESTED / CLEANING / RESERVED
    val waiterName: String? = null,
    val waiterId: String? = null,
    val activeOrderId: String? = null,
    val guestsCount: Int? = null,
    val updatedAt: Long? = null,
    val createdAt: Long? = null,
    val notes: String? = null,
    val synced: Boolean? = null
)
