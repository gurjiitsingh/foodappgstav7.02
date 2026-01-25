package com.it10x.foodappgstav7_02.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_02.data.online.models.OrderProductData
import com.it10x.foodappgstav7_02.data.online.models.formattedTime
import com.it10x.foodappgstav7_02.data.online.models.repository.OrdersRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_02.printer.FirestorePrintMapper
import com.it10x.foodappgstav7_02.printer.ReceiptFormatter
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.it10x.foodappgstav7_02.data.mapper.OnlineOrderMapper
class OnlineOrdersViewModel(
    private val printerManager: PrinterManager
) : ViewModel() {

    private val repo = OrdersRepository()


    suspend fun getOrderItems(orderId: String): List<OrderProductData> {
        return repo.getOrderProducts(orderId)
    }

    private val _orderItems = MutableStateFlow<Map<String, List<OrderProductData>>>(emptyMap())
    val orderItems: StateFlow<Map<String, List<OrderProductData>>> = _orderItems

    fun loadOrderItems(orderId: String) {
        viewModelScope.launch {
            val items = repo.getOrderProducts(orderId)
            _orderItems.value = _orderItems.value + (orderId to items)
        }
    }
    val pageIndex = MutableStateFlow(0)
    private val _orders = MutableStateFlow<List<OrderMasterData>>(emptyList())
    val orders: StateFlow<List<OrderMasterData>> = _orders

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val limit = 10

    // -----------------------------
    // PRINT ORDER (KITCHEN + BILLING)
    // -----------------------------
    fun printOrder(order: OrderMasterData) {

     //   Log.d("PRINT_SOURCE", "🔥 OrdersViewModel.printOrder CALLED")
        viewModelScope.launch {

            val items = repo.getOrderProducts(order.id)
            Log.e("PRINT", "Order No. ${order.srno}")
            if (items.isEmpty()) {
                Log.e("PRINT", "No items for order ${order.srno}")
                return@launch
            }

            //BILL PRINT ONLINE ORDER WHEN BUTTON PRESSED
            val printOrder = FirestorePrintMapper.map(order, items)
            printerManager.printTextNew(PrinterRole.BILLING, printOrder)

            kotlinx.coroutines.delay(2_000)


            //KITCHEN PRINT ONLINE ORDER WHEN BUTTON PRESSED
            val kotItems = OnlineOrderMapper.toKotItems(items)

            printerManager.printTextKitchen(
                PrinterRole.KITCHEN,
                sessionKey = order.srno.toString(),
                orderType = "Online order",
                kotItems ){
                Log.d("PRINT", "Kitchen print success=$it")
            }


        }
    }




fun loadFirstPage() {
    viewModelScope.launch {
        _loading.value = true

        pageIndex.value = 0

        _orders.value = repo.getFirstPage(limit.toLong())
            .sortedByDescending { it.createdAt?.seconds ?: 0L }

        _loading.value = false
    }
}

fun loadNextPage() {
    viewModelScope.launch {
        _loading.value = true

        pageIndex.value++

        _orders.value = repo.getNextPage(limit.toLong())
            .sortedByDescending { it.createdAt?.seconds ?: 0L }

        _loading.value = false
    }
}

fun loadPrevPage() {
    viewModelScope.launch {
        _loading.value = true

        if (pageIndex.value > 0)
            pageIndex.value--

        _orders.value = repo.getPrevPage(limit.toLong())
            .sortedByDescending { it.createdAt?.seconds ?: 0L }

        _loading.value = false
    }
}



    // -----------------------------
// BILLING RECEIPT (SAFE FORMAT)
// -----------------------------

    private fun buildBillingReceipt(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): String {
        // ESC/POS force left align
        val alignLeft = "\u001B\u0061\u0000"

        val itemsBlock = if (items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(5) +
                        "ITEM".take(12).padEnd(12) +
                        "PRICE".padStart(7) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(32)

            val lines = items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(2)
                val name = item.name.take(17).padEnd(17)
                val price = formatAmount(toDouble(item.price)).padStart(6)//right align
                // val price = formatAmount(toDouble(item.price)).padEnd(6)//left align
                val total = formatAmount(toDouble(item.itemSubtotal)).padStart(7)

                qty +  name +  price + total
            }

            "$header\n$divider\n$lines"
        }

        // Build discount block dynamically
        val discountBlock = buildDiscountBlock(order)

        return buildString {
            append(alignLeft)
            append(
                """
------------------------------
FOOD APP 4
------------------------------
Order No : ${order.srno}
Customer : ${order.customerName.ifBlank { "Walk-in" }}
Date     : ${order.formattedTime()}
------------------------------
$itemsBlock
------------------------------
${totalLine("Item Total", toDouble(order.itemTotal))}
${totalLine("Delivery Cost", toDouble(order.deliveryFee))}
$discountBlock
${totalLine("Sub Total", toDouble(order.subTotal))}
${totalLine("GST ", toDouble(order.taxTotal))}

------------------------------
${totalLine("GRAND TOTAL", toDouble(order.grandTotal))}
------------------------------
Thank You!


""".trimIndent()
            )
        }
    }

    private fun buildBillingReceipt2(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): String {

        // ESC/POS force left align
        val alignLeft = "\u001B\u0061\u0000"

        val orderNoLine =
            "Order No : ${btSafe(order.srno.toString(), 12)}"

        val customerLine =
            "Customer : ${btSafe(order.customerName.ifBlank { "Walk-in" }, 18)}"

        val itemsBlock = if (items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(16) +
                        "PRICE".padStart(6) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(32)

            val lines = items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd  (4)
                val name = item.name.take(16).padEnd(16)
                val price = formatAmount(toDouble(item.price)).padStart(6)
                val total = formatAmount(toDouble(item.itemSubtotal)).padStart(6)

                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(alignLeft)
            append(
                """
------------------------------
FOOD APP 5
------------------------------


------------------------------

------------------------------

------------------------------
Thank You!


""".trimIndent()
            )
        }
    }

    private fun buildBillingReceipt1(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): String {

        // ESC/POS left align
        val alignLeft = "\u001B\u0061\u0000"

        return buildString {
            append(alignLeft)
            append(
                """
------------------------------
FOOD APP TEST
------------------------------
Order No : 123
Customer : TEST
------------------------------
QTY ITEM            PRICE TOTAL
--------------------------------
1   Burger          100.00 100.00
2   Fries            50.00 100.00
--------------------------------
TOTAL : 200.00
------------------------------
Thank You!


""".trimIndent()
            )
        }
    }






    // -----------------------------
// KITCHEN SLIP (SAFE FORMAT)
// -----------------------------
    private fun buildKitchenReceipt(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): String {

        val alignLeft = "\u001B\u0061\u0000"

        val itemsBlock = if (items.isEmpty()) {
            "No items"
        } else {
            items.joinToString("\n") { item ->
                "${item.quantity.toString().padEnd(3)} ${item.name}"
            }
        }

        return buildString {
            append(alignLeft)
            append(
                """
******** KITCHEN ********
Order No : ${order.srno}
------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }



    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return "" // skip zero values
        val left = label.padEnd(14)
        val right = formatAmount(value).padStart(18)
        return left + right
    }
    private fun formatAmount(value: Double?): String =
        "%.2f".format(value ?: 0.0)

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

    private fun btSafe(text: String, max: Int): String {
        return text
            .replace(Regex("[^A-Za-z0-9 ]"), "") // remove Unicode
            .trim()
            .take(max)
    }

    // -----------------------------
    // DISCOUNT BLOCK
    // -----------------------------
    private fun buildDiscountBlock(order: OrderMasterData): String {
        val pickup = toDouble(order.pickUpDiscount)
        val flat = toDouble(order.couponFlat)
        val coupon = toDouble(order.couponPercent)

        return buildString {
            if (pickup > 0) appendLine(totalLine("Pickup Dis", pickup))
            if (flat > 0) appendLine(totalLine("Flat Dis", flat))
            if (coupon > 0) appendLine(totalLine("Coupon Dis", coupon))
        }
    }
}
