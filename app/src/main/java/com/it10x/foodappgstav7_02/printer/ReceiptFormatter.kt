package com.it10x.foodappgstav7_02.printer

import android.util.Log
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.print.OutletInfo

// -----------------------------
// PRINT MODELS (ONE TRUTH)
// -----------------------------


// -----------------------------
// RECEIPT FORMATTER
// -----------------------------

object ReceiptFormatter {

    private const val LINE_WIDTH = 32
    private const val ALIGN_LEFT = "\u001B\u0061\u0000"





    // -----------------------------
    // BILLING RECEIPT
    // -----------------------------
    fun billing(order: PrintOrder, outletInfo: OutletInfo): String {

        val LINE_WIDTH = 32
        Log.d("RECEIPT_FORMATTER", "billing48() called for orderNo=${order.orderNo}")
        val outletHeader = buildOutletHeader(outletInfo, LINE_WIDTH)

        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(16) +
                        "PRICE".padStart(6) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(LINE_WIDTH)

            val lines = order.items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(4)
                val name = item.name.take(16).padEnd(16)
                val price = format(item.price).padStart(6)
                val total = format(item.subtotal).padStart(6)
                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
------------------------------
$headerBlock
------------------------------
$headerBlock
------------------------------
$itemsBlock
------------------------------
${totalLine("Item Total", order.itemTotal)}
${totalLine("Delivery", order.deliveryFee)}
${totalLine("Discount", order.discount)}
${totalLine("Tax", order.tax)}
------------------------------
${totalLine("GRAND TOTAL", order.grandTotal)}
------------------------------
Thank You!


""".trimIndent()
            )
        }
    }





    fun billing48(order: PrintOrder, outletInfo: OutletInfo): String {

        val LINE_WIDTH = 48
        Log.d("RECEIPT_FORMATTER", "billing48() called for orderNo=${order.orderNo}")
        val outletHeader = buildOutletHeader(outletInfo, LINE_WIDTH)




        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            // 48 chars total → distribute: qty(4) + name(26) + price(8) + total(10)
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(26) +
                        "PRICE".padStart(8) +
                        "TOTAL".padStart(10)

            val divider = "-".repeat(LINE_WIDTH)

            val lines = order.items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(4)
                val name = item.name.take(26).padEnd(26)
                val price = format(item.price).padStart(8)
                val total = format(item.subtotal).padStart(10)
                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
------------------------------------------------
$outletHeader
------------------------------------------------
$headerBlock
------------------------------------------------
$itemsBlock
------------------------------------------------
${totalLine48("Item Total", order.itemTotal)}
${totalLine48("Delivery", order.deliveryFee)}
${totalLine48("Discount", order.discount)}
${totalLine48("Tax", order.tax)}
------------------------------------------------
${totalLine48("GRAND TOTAL", order.grandTotal)}
------------------------------------------------
Thank You!


""".trimIndent()
            )
        }
    }




    fun billing83(order: PrintOrder, title: String = "FOOD APP"): String {
        return billingWithWidth(order, title, lineWidth = 48) // 48 chars for 83mm
    }


    // -----------------------------
    // KITCHEN RECEIPT
    // -----------------------------

    fun kitchen(items: List<PosKotItemEntity>, title: String = "KITCHEN"): String {

        val itemsBlock = if (items.isEmpty()) {
            "No items"
        } else {
            items.joinToString("\n") {
                "${it.quantity.toString().padEnd(3)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********

------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }
    fun kitchen1(order: PrintOrder, title: String = "KITCHEN"): String {

        val itemsBlock = if (order.items.isEmpty()) {
            "No items"
        } else {
            order.items.joinToString("\n") {
                "${it.quantity.toString().padEnd(3)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********
Order No : ${order.orderNo}
------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }



    fun kitchen83(order: PrintOrder, title: String = "KITCHEN"): String {
        val itemsBlock = if (order.items.isEmpty()) {
            "No items"
        } else {
            order.items.joinToString("\n") {
                "${it.quantity.toString().padEnd(4)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********
Order No : ${order.orderNo}
${"-".repeat(48)}
$itemsBlock
${"-".repeat(48)}


""".trimIndent()
            )
        }
    }


    // -----------------------------
    // HEADER LOGIC (IMPORTANT)
    // -----------------------------
    private fun buildHeaderBlock(order: PrintOrder): String {

        val base = mutableListOf<String>()

        // --- Common fields ---
        base.add("Order No : ${order.orderNo}")
        base.add("Customer : ${order.customerName.ifBlank { "Walk-in" }}")
        base.add("Date     : ${order.dateTime}")

        when (order.orderType) {

            // -----------------------------
            // DINE-IN ORDERS
            // -----------------------------
            "DINE_IN" -> {
                order.tableNo?.takeIf { it.isNotBlank() }?.let {
                    base.add("Table    : $it")
                }
            }

            // -----------------------------
            // TAKEAWAY ORDERS
            // -----------------------------
            "TAKEAWAY" -> {
                // Only show phone if filled
                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }

            // -----------------------------
            // DELIVERY / ONLINE ORDERS
            // -----------------------------
            "DELIVERY", "ONLINE" -> {
                val addressLines = mutableListOf<String>()

                order.dAddressLine1?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dAddressLine2?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dLandmark?.takeIf { it.isNotBlank() }?.let { addressLines.add("Landmark: $it") }

                // City + Zip
                listOfNotNull(order.dCity, order.dZipcode)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                    ?.let { addressLines.add(it) }

                if (addressLines.isNotEmpty()) {
                    base.add("Address  :")
                    base.addAll(addressLines)
                }

                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }
        }

        return base.joinToString("\n")
    }



    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return ""
        val left = label.padEnd(14)
        val right = format(value).padStart(18)
        return left + right
    }

    private fun format(value: Double): String = "%.2f".format(value)


    fun posKitchen(
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        title: String = "KITCHEN"
    ): String {

        val time = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val header = buildString {
            append("******** $title ********\n")
            append("Type  : $orderType\n")
            append("Ref   : $sessionKey\n")
            append("Time  : $time\n")
            append("------------------------\n")
        }

        val itemsBlock =
            if (items.isEmpty()) {
                "No items\n"
            } else {
                items.joinToString("\n") {
                    "${it.quantity.toString().padEnd(3)} ${it.name}"
                } + "\n"
            }

        return buildString {
            append(ALIGN_LEFT)
            append(header)
            append(itemsBlock)
            append("------------------------\n\n")
        }
    }














    // -----------------------------
// INTERNAL HELPERS (dynamic lineWidth)
// -----------------------------
    private fun billingWithWidth(order: PrintOrder, title: String, lineWidth: Int): String {

        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(5) +               // slightly more space for qty
                        "ITEM".padEnd(lineWidth - 20) + // dynamic item width
                        "PRICE".padStart(7) +
                        "TOTAL".padStart(7)

            val divider = "-".repeat(lineWidth)

            val lines = order.items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(5)
                val name = item.name.take(lineWidth - 20).padEnd(lineWidth - 20)
                val price = format(item.price).padStart(7)
                val total = format(item.subtotal).padStart(7)
                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
${"-".repeat(lineWidth)}
$title
${"-".repeat(lineWidth)}
$headerBlock
${"-".repeat(lineWidth)}
$itemsBlock
${"-".repeat(lineWidth)}
${totalLineWidth("Item Total", order.itemTotal, lineWidth)}
${totalLineWidth("Delivery", order.deliveryFee, lineWidth)}
${totalLineWidth("Discount", order.discount, lineWidth)}
${totalLineWidth("Tax", order.tax, lineWidth)}
${"-".repeat(lineWidth)}
${totalLineWidth("GRAND TOTAL", order.grandTotal, lineWidth)}
${"-".repeat(lineWidth)}
Thank You!


""".trimIndent()
            )
        }
    }

    // totalLine helper for dynamic width
    private fun totalLineWidth(label: String, value: Double, lineWidth: Int): String {
        if (value == 0.0) return ""
        val left = label.padEnd(lineWidth - 12)
        val right = format(value).padStart(12)
        return left + right
    }


    private fun totalLine48(label: String, amount: Double): String {
        val formatted = format(amount)
        // label left-aligned, amount right-aligned to total 48 characters
        val space = 48 - label.length - formatted.length
        return label + " ".repeat(if (space > 0) space else 1) + formatted
    }


    private fun buildOutletHeader(info: OutletInfo, width: Int): String {
        val lines = mutableListOf<String>()
        if (info.name.isNotBlank()) lines += centerText(info.name, width)
        info.addressLine1
            ?.takeIf { it.isNotBlank() }
            ?.let { address ->
                lines += address.take(width)
            }
       if(width==32){
            info.addressLine2?.let { lines += it.take(width) }
            info.addressLine3?.let { lines += it.take(width) }
            info.city?.let { lines += it.take(width) }
            info.phone?.let { lines += "Phone: $it" }
           info.phone2?.let { lines += " $it" }
            info.email?.let { lines += "Email: $it" }
            info.web?.let { lines += "Web: $it" }
            info.gst?.let { lines += "GST: $it" }
            info.footerNote?.let { lines += it.take(width) }
        }

        if(width==48){
            info.addressLine2?.let { lines += it.take(width) }
            info.addressLine3?.let { lines += it.take(width) }
            info.city?.let { lines += it.take(width) }
            val phone1 = info.phone?.takeIf { it.isNotBlank() }
            val phone2 = info.phone2?.takeIf { it.isNotBlank() }

            if (phone1 != null && phone2 != null) {
                // Both phones available
                lines += "Phone: $phone1, $phone2".take(width)
            } else if (phone1 != null) {
                // Only first phone
                lines += "Phone: $phone1".take(width)
            } else if (phone2 != null) {
                // Only second phone
                lines += "Phone: $phone2".take(width)
            }
            info.email?.let { lines += "Email: $it" }
            info.web?.let { lines += "Web: $it" }
            info.gst?.let { lines += "GST: $it" }
            info.footerNote?.let { lines += it.take(width) }
        }

        return lines.joinToString("\n")
    }

    private fun centerText(text: String, width: Int): String {
        val pad = (width - text.length) / 2
        return " ".repeat(maxOf(pad, 0)) + text
    }


}
