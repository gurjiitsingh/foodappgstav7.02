package com.it10x.foodappgstav5_1.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.models.createdAtMillis
import com.it10x.foodappgstav5_1.printer.PrinterManager
import com.it10x.foodappgstav5_1.viewmodel.OrdersViewModel
import com.it10x.foodappgstav5_1.viewmodel.RealtimeOrdersViewModel
import com.it10x.foodappgstav5_1.ui.orders.OrderDetailScreen
@Composable
fun OrdersScreen(

    printerManager: PrinterManager,
    ordersViewModel: OrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel
) {
    var selectedOrder by remember { mutableStateOf<OrderMasterData?>(null) }

    if (selectedOrder != null) {
        // Show detail screen
        OrderDetailScreen(
            order = selectedOrder!!,
            ordersViewModel = ordersViewModel,
            realtimeOrdersViewModel = realtimeOrdersViewModel,
            onBack = { selectedOrder = null }
        )
        return
    }



    // -----------------
    // Original orders list
    // -----------------
    LaunchedEffect(Unit) { ordersViewModel.loadFirstPage() }

    val pagedOrders by ordersViewModel.orders.collectAsState()
    val realtimeOrders by realtimeOrdersViewModel.realtimeOrders.collectAsState()
    val loading by ordersViewModel.loading.collectAsState()
    val pageIndex by ordersViewModel.pageIndex.collectAsState()

    val combinedOrders = remember(realtimeOrders, pagedOrders, pageIndex) {
        val isFirstPage = pageIndex == 0
        val list = if (isFirstPage) {
            val realtimeIds = realtimeOrders.map { it.id }.toSet()
            realtimeOrders + pagedOrders.filter { it.id !in realtimeIds }
        } else pagedOrders

        list.sortedByDescending { it.createdAtMillis() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
//        Text("POS Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//        Spacer(Modifier.height(8.dp))

        when {
            loading && combinedOrders.isEmpty() -> Text("Loading orders...")
            combinedOrders.isEmpty() -> Text("No orders found")
            else -> {
                PosOrderTableHeader()
                LazyColumn {
                    items(combinedOrders, key = { it.id }) { order ->
                        PosOrderTableRow(
                            order = order,
                            onOrderClick = { selectedOrder = order },
                            onPrintClick = { ordersViewModel.printOrder(order) }

                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { ordersViewModel.loadPrevPage() }, enabled = !loading) { Text("← Previous") }
                    Button(onClick = { ordersViewModel.loadNextPage() }, enabled = !loading) { Text("Next →") }
                }
            }
        }
    }
}

