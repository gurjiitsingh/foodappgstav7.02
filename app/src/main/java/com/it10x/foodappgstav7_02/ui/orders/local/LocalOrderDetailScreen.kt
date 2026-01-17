package com.it10x.foodappgstav7_02.ui.orders.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocalOrderDetailScreen(
    viewModel: LocalOrderDetailViewModel,
    onBack: () -> Unit
) {
    val order by viewModel.orderInfo.collectAsState()
    val products by viewModel.products.collectAsState()

    val subtotal by viewModel.subtotal.collectAsState()
    val tax by viewModel.taxTotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ================= HEADER =================
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Order Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // ================= ORDER + ADDRESS (2 COLUMNS) =================
        order?.let { o ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {

                    // ---------- LEFT : ORDER INFO ----------
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text("Order Info", fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(6.dp))

                        Text("Order #: ${o.srno}")
                        Text(
                            rememberDateFormatter().format(Date(o.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(6.dp))

                        Text("Type: ${o.orderType}")
                        o.tableNo?.let {
                            Text("Table: $it")
                        }

                        Spacer(Modifier.height(6.dp))

                        Text("Payment: ${o.paymentType}")
                        Text(
                            "Status: ${o.orderStatus}",
                            fontWeight = FontWeight.Medium,
                            color = when (o.orderStatus) {
                                "NEW" -> Color(0xFF1976D2)
                                "ACCEPTED" -> Color(0xFF388E3C)
                                "COMPLETED" -> Color(0xFF2E7D32)
                                "CANCELLED" -> Color(0xFFD32F2F)
                                else -> Color.DarkGray
                            }
                        )
                    }

                    // ---------- RIGHT : DELIVERY ADDRESS ----------
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delivery Address", fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(6.dp))

                        Text(
                            o.customerName ?: "Walk-in",
                            fontWeight = FontWeight.Medium
                        )

                        o.customerPhone?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        listOfNotNull(
                            o.dAddressLine1,
                            o.dAddressLine2,
                            listOfNotNull(
                                o.dCity,
                                o.dState,
                                o.dZipcode
                            ).joinToString(" ").takeIf { it.isNotBlank() },
                            o.dLandmark?.let { "Near $it" }
                        ).forEach { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // ================= ITEMS (SCROLLABLE) =================
        Text("Items", style = MaterialTheme.typography.titleMedium)
        Divider(Modifier.padding(vertical = 4.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)          // ⭐ makes list scroll
                .fillMaxWidth()
        ) {
            items(products, key = { it.id }) { item ->
                OrderProductRow(item)
                Divider(color = Color(0xFFE0E0E0))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ================= TOTALS (FIXED BOTTOM) =================
        OrderTotals(
            subtotal = subtotal,
            tax = tax,
            grandTotal = grandTotal
        )
    }
}

@Composable
fun OrderProductRow(item: PosOrderItemEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {

            // LEFT SIDE: NAME + DETAILS
            Column(
                modifier = Modifier.weight(1f)
            ) {

                // 🔹 Item name
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(2.dp))

                // 🔹 Quantity × base price
                Text(
                    text = "${item.quantity} × ₹${"%.2f".format(item.basePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                // 🔹 Variant indicator (if any)
                if (item.isVariant && !item.parentId.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Variant item",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF616161)
                    )
                }

                // 🔹 Tax info
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "GST ${item.taxRate}% (${item.taxType})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // RIGHT SIDE: TOTAL
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${"%.2f".format(item.finalTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "₹${"%.2f".format(item.finalPricePerItem)} / item",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}


@Composable
fun OrderTotals(
    subtotal: Double,
    tax: Double,
    grandTotal: Double
) {
    Column {
        TotalRow("Subtotal", subtotal)
        TotalRow("GST", tax)
        Divider(Modifier.padding(vertical = 4.dp))
        TotalRow("Grand Total", grandTotal, bold = true)
    }
}

@Composable
fun TotalRow(
    label: String,
    value: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            "₹${"%.2f".format(value)}",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
}
