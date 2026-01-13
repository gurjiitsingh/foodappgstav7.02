package com.it10x.foodappgstav5_1.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.models.OrderProductData
import com.it10x.foodappgstav5_1.data.models.formattedTime
import com.it10x.foodappgstav5_1.viewmodel.OrdersViewModel
import com.it10x.foodappgstav5_1.viewmodel.RealtimeOrdersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: OrderMasterData,
    ordersViewModel: OrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var orderItems by remember { mutableStateOf<List<OrderProductData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Load items when screen opens
    LaunchedEffect(order.id) {
        loading = true
        orderItems = ordersViewModel.getOrderItems(order.id) // suspend function
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #${order.srno}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {

                // --------------------------
                // CUSTOMER INFO
                // --------------------------
                Text("Customer: ${order.customerName.ifBlank { "Walk-in" }}",
                    style = MaterialTheme.typography.titleMedium)
                order.customerPhone?.let { Text("Phone: $it", style = MaterialTheme.typography.bodyMedium) }
                order.email?.let { Text("Email: $it", style = MaterialTheme.typography.bodyMedium) }
                order.addressId.takeIf { it.isNotBlank() }?.let { Text("Address: $it", style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(8.dp))

                // --------------------------
                // ORDER INFO
                // --------------------------
                Text("Source: ${order.source ?: "POS"}", style = MaterialTheme.typography.bodyMedium)
                Text("Order Type: ${order.orderType ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                order.tableNo?.let { Text("Table: $it", style = MaterialTheme.typography.bodyMedium) }
                Text("Status: ${order.orderStatus ?: "NEW"}", style = MaterialTheme.typography.bodyMedium)
                Text("Payment: ${order.paymentType} • ${order.paymentStatus ?: "PENDING"}",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                // --------------------------
                // AMOUNTS
                // --------------------------
                Text("Item Total: ${order.itemTotal}", style = MaterialTheme.typography.bodyMedium)
                order.discountTotal?.let { Text("Discount: $it", style = MaterialTheme.typography.bodyMedium) }
                order.subTotal?.let { Text("Subtotal: $it", style = MaterialTheme.typography.bodyMedium) }
                order.taxTotal?.let { Text("Tax: $it", style = MaterialTheme.typography.bodyMedium) }
                order.deliveryFee?.let { Text("Delivery Fee: $it", style = MaterialTheme.typography.bodyMedium) }
                order.grandTotal?.let { Text("Grand Total: $it", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(12.dp))

                // --------------------------
                // ORDER ITEMS
                // --------------------------
                if (loading) {
                    Text("Loading items...")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(orderItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity} x ${item.name}")
                                Text("${item.itemSubtotal}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --------------------------
                // ACTION BUTTONS
                // --------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { ordersViewModel.printOrder(order) }) {
                        Text("Print")
                    }

                    Button(
                        onClick = {
                            scope.launch { realtimeOrdersViewModel.acknowledgeOrder(order.id) }
                        },
                        enabled = order.acknowledged != true
                    ) {
                        Text(if (order.acknowledged == true) "Acknowledged" else "Acknowledge")
                    }
                }
            }
        }
    )
}
