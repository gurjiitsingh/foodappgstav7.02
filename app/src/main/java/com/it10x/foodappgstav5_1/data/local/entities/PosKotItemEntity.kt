package com.it10x.foodappgstav5_1.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_kot_items",
    indices = [
        Index(value = ["kotBatchId"]),
        Index(value = ["tableNo"]),
        Index(value = ["productId"])
    ]
)
data class PosKotItemEntity(
    @PrimaryKey
    val id: String,

    val kotBatchId: String,
    val tableNo: String?,

    val productId: String,
    val name: String,
    val categoryId: String,

    val parentId: String?,
    val isVariant: Boolean,

    val basePrice: Double,
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,

    val status: String, // ✅ NEW

    val createdAt: Long
)
