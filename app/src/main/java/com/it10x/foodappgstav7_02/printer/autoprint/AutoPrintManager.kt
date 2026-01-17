package com.it10x.foodappgstav7_02.printer

import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_02.data.online.models.OrderProductData
import com.it10x.foodappgstav7_02.data.online.models.repository.OrdersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val printingOrders = mutableSetOf<String>()
class AutoPrintManager(
    private val printerManager: PrinterManager,
    private val ordersRepository: OrdersRepository
) {

    private val printingOrders = mutableSetOf<String>()

    fun onNewOrder(order: OrderMasterData) {

        // ⛔ Already printed in DB
        if (order.printed == true) return

        synchronized(printingOrders) {
            if (printingOrders.contains(order.id)) return
            printingOrders.add(order.id)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Wait for items
                var itemsReady = false
                var items: List<OrderProductData> = emptyList()
                repeat(10) { attempt ->
                    items = ordersRepository.getOrderProducts(order.id)
                    if (items.isNotEmpty()) {
                        itemsReady = true
                        return@repeat
                    }
                    delay(1000)
                }

                if (!itemsReady) return@launch

                // 🖨 Print billing + kitchen
                val billingReceipt = ReceiptFormatter.billing(
                    FirestorePrintMapper.map(order, items),
                    title = "FOOD APP"
                )
                val kitchenReceipt = ReceiptFormatter.kitchen(
                    FirestorePrintMapper.map(order, items)
                )

                printerManager.printText(PrinterRole.BILLING, billingReceipt) { }
                delay(10_000)
                printerManager.printText(PrinterRole.KITCHEN, kitchenReceipt) { }

                // ✅ Mark printed
                ordersRepository.markOrderAsPrinted(order.id)

            } catch (_: Exception) {
            } finally {
                synchronized(printingOrders) { printingOrders.remove(order.id) }
            }
        }
    }
}

