package com.it10x.foodappgstav5_1.printer

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
    fun billing(order: PrintOrder, title: String = "FOOD APP"): String {

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
$title
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

    // -----------------------------
    // KITCHEN RECEIPT
    // -----------------------------
    fun kitchen(order: PrintOrder, title: String = "KITCHEN"): String {

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

    // -----------------------------
    // HEADER LOGIC (IMPORTANT)
    // -----------------------------
    private fun buildHeaderBlock(order: PrintOrder): String {

        val base = mutableListOf(
            "Order No : ${order.orderNo}",
            "Customer : ${order.customerName.ifBlank { "Walk-in" }}",
            "Date     : ${order.dateTime}"
        )

        when (order.orderType) {

            "DINE_IN" -> {
                order.tableNo?.takeIf { it.isNotBlank() }?.let {
                    base.add("Table    : $it")
                }
            }

            "DELIVERY", "ONLINE" -> {
                listOfNotNull(
                    order.dAddressLine1,
                    order.dAddressLine2,
                    listOfNotNull(order.dCity, order.dZipcode).joinToString(" ").takeIf { it.isNotBlank() },
                   // order.dState,
                    order.customerPhone.let { "Phone $it" },
                    order.dLandmark?.let { "$it" }
                ).takeIf { it.isNotEmpty() }?.let { addressLines ->
                    base.add("Address  :")
                    base.addAll(addressLines)
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
}
