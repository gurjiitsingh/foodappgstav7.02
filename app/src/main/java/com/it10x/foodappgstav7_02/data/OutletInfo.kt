package com.it10x.foodappgstav7_02.data.print

data class OutletInfo(
    val name: String = "",
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val phone2: String? = null,
    val email: String? = null,
    val web: String? = null,
    val gst: String? = null,
    val footerNote: String? = null
)