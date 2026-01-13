package com.it10x.foodappgstav5_1.data.models

import com.google.firebase.Timestamp

/**
 * OrderMasterData (NEW SYSTEM)
 *
 * Only standard, clean fields.
 * Used for POS printing, Firestore, and Web orders.
 */
data class OrderMasterData(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================
    var id: String = "",
    var srno: Int = 0,
    var source: String? = null,

    // =====================================================
    // CUSTOMER
    // =====================================================
    var customerId: String? = null,   // ✅ NEW
    var customerName: String = "",
    var email: String = "",
    var addressId: String = "",
    var customerPhone: String? = null,

    // =====================================================
    // CUSTOMER ADDRESS (PRINT SNAPSHOT)
    // =====================================================
    var dAddressLine1: String? = null,
    var dAddressLine2: String? = null,
    var dCity: String? = null,
    var dState: String? = null,
    var dZipcode: String? = null,
    var dLandmark: String? = null,

    // =====================================================
    // ORDER TYPE
    // =====================================================
    var orderType: String? = null,
    var tableNo: String? = null,

    // =====================================================
    // AMOUNTS
    // =====================================================
    var itemTotal: Double = 0.0,
    var subTotal: Double? = null,
    var discountTotal: Double? = null,
    var taxTotal: Double? = null,
    var deliveryFee: Double? = null,
    var grandTotal: Double? = null,

    // =====================================================
    // PAYMENT
    // =====================================================
    var paymentType: String = "",
    var paymentStatus: String? = null,

    // =====================================================
    // ORDER FLOW
    // =====================================================
    var orderStatus: String? = null,

    // =====================================================
    // OUTLET (MULTI-LOCATION)
    // =====================================================
    var outletId: String? = null,
    var outletName: String? = null,

    // =====================================================
    // POS HELPERS
    // =====================================================
    var productsCount: Int? = null,
    var notes: String? = null,

    // =====================================================
    // TIMESTAMPS
    // =====================================================
    var createdAt: Timestamp? = null,

    // =====================================================
    // AUTOMATION
    // =====================================================
    var printed: Boolean? = null,
    var acknowledged: Boolean? = null,

    // =====================================================
    // SYNC CONTROL
    // =====================================================
    var syncStatus: String? = null,

    // =====================================================
    // LEGACY (KEEP)
    // =====================================================

   var couponFlat: Double? = null,
   var pickUpDiscount: Double? = null,
   var couponPercent: Double? = null,
)





// Extension to get milliseconds from Firestore Timestamp
fun OrderMasterData.createdAtMillis(): Long {
    return createdAt?.toDate()?.time ?: 0L
}

// Extension to format timestamp for printing
fun OrderMasterData.formattedTime(): String {
    val millis = createdAtMillis()
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

