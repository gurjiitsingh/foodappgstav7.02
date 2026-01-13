package com.it10x.foodappgstav5_1.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_products",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["parentId"]),
        Index(value = ["type"]),
        Index(value = ["baseProductId"])
    ]
)
data class PosProductEntity(

    // =====================================================
    // CORE IDENTITY
    // =====================================================
    @PrimaryKey
    val id: String,                  // Firestore ID (same across systems)

    val name: String,
    val price: Double,

    // =====================================================
    // VARIANT SYSTEM (CRITICAL)
    // =====================================================
    val type: String?,               // parent | variant | null
    val parentId: String?,           // Parent product ID (for variants)
    val hasVariants: Boolean,

    // =====================================================
    // TAX (CALCULATION SAFE)
    // =====================================================
    val taxRate: Double?,            // e.g. 5, 12, 18
    val taxType: String?,            // inclusive | exclusive

    // =====================================================
    // CATEGORY & SORTING
    // =====================================================
    val categoryId: String,
    val sortOrder: Int,

    // =====================================================
    // INVENTORY
    // =====================================================
    val stockQty: Int,
    val discountPrice: Double?,

    // =====================================================
    // IMAGE / UI (LIGHTWEIGHT)
    // =====================================================
    val image: String?,

    // =====================================================
    // SYNC META
    // =====================================================
    val baseProductId: String?,      // ORIGINAL product ID (for sync safety)
    val updatedAt: Long              // Used to detect changes
)
