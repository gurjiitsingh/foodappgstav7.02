package com.it10x.foodappgstav7_02.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "cart",
    indices = [
        Index(value = ["productId", "tableId"], unique = true)
    ]
)
data class PosCartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val productId: String,
    val name: String,
    val categoryId: String,

    val parentId: String?,
    val isVariant: Boolean,

    val basePrice: Double,
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,

    // 🔑 NEW (PRIMARY POS SESSION)
    val sessionId: String,

    // 🪑 OPTIONAL (only for DINE_IN)
    val tableId: String?,

    // ⭐ NEW
    val sentToKitchen: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
