package com.it10x.foodappgstav5_1.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_order_items",
    indices = [
        Index(value = ["orderMasterId"]),
        Index(value = ["productId"]),
        Index(value = ["parentId"])
    ]
)
data class PosOrderItemEntity(

    // =====================================================
    // CORE IDENTITY
    // =====================================================
    @PrimaryKey
    val id: String,                  // UUID (generated on POS)

    val orderMasterId: String,       // FK → pos_order_master.id
    val productId: String,           // Original product ID (Firestore)

    // =====================================================
    // SNAPSHOT PRODUCT INFO (NEVER CHANGES)
    // =====================================================
    val name: String,
    val categoryId: String,

    // =====================================================
    // VARIANT SNAPSHOT (CRITICAL)
    // =====================================================
    val parentId: String?,           // Parent product ID (if variant)
    val isVariant: Boolean,

    // =====================================================
    // PRICING (BASE)
    // =====================================================
    val basePrice: Double,           // Price before tax (single item)
    val quantity: Int,
    val itemSubtotal: Double,        // basePrice * quantity

    // =====================================================
    // TAX SNAPSHOT (FINAL)
    // =====================================================
    val taxRate: Double,
    val taxType: String,             // inclusive | exclusive

    val taxAmountPerItem: Double,    // tax per item
    val taxTotal: Double,            // taxAmountPerItem * quantity

    // =====================================================
    // FINAL VALUES (NEVER RECALCULATE)
    // =====================================================
    val finalPricePerItem: Double,   // basePrice + tax
    val finalTotal: Double,          // finalPricePerItem * quantity

    // =====================================================
    // SYNC & AUDIT
    // =====================================================
    val source: String = "POS",
    val createdAt: Long              // System.currentTimeMillis()
)
