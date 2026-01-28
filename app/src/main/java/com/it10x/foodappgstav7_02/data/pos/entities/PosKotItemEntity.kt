package com.it10x.foodappgstav7_02.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_kot_items",
    indices = [
        Index(value = ["kotBatchId"]),
        Index(value = ["sessionId"]),   // ✅ MOST IMPORTANT
        Index(value = ["tableNo"]),
        Index(value = ["productId"])
    ]
)
data class PosKotItemEntity(
    @PrimaryKey
    val id: String,

    val sessionId: String?,             // ✅ ADD THIS
    val kotBatchId: String,

    val tableNo: String?,              // UI / print only

    val productId: String,
    val name: String,
    val categoryId: String,

    val parentId: String?,
    val isVariant: Boolean,

    val basePrice: Double,
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,

    val status: String,                // PENDING / DONE
    val isPrinted: Boolean,

    val createdAt: Long
)
