package com.it10x.foodappgstav5_1.data.local.entities.config

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlet_config")
data class OutletEntity(

    @PrimaryKey
    val outletId: String,

    val outletName: String,

    // ---------- ADDRESS ----------
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,     // ⭐ NEW
    val city: String,
    val state: String?,
    val zipcode: String?,
    val country: String?,

    // ---------- TAX ----------
    val taxType: String?,
    val gstVatNumber: String?,

    // ---------- CONTACT ----------
    val phone: String,
    val phone2: String?,           // ⭐ already there
    val email: String?,            // ⭐ already there
    val web: String?,              // ⭐ NEW

    // ---------- PRINTER ----------
    val printerWidth: Int,
    val printerName: String?,
    val footerNote: String?,

    // ---------- STATUS ----------
    val isActive: Boolean,

    // ---------- META ----------
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
