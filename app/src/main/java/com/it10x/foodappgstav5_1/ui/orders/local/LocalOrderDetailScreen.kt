package com.it10x.foodappgstav5_1.ui.orders.local

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderItemEntity
import com.it10x.foodappgstav5_1.data.local.entities.PosOrderMasterEntity
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

        // 🔙 HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back"
                )
            }
            TextButton(onClick = onBack) { Text("Back") }
            Text("Order Details", style = MaterialTheme.typography.titleLarge)




          //  TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        // 🗂 ORDER INFO
        order?.let { o ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    // 🔝 Order number + date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Order #${o.srno}",
                            fontWeight = FontWeight.Bold
                        )

                        val sdf = rememberDateFormatter()


                        Text(
                            sdf.format(Date(o.createdAt)),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // 👤 CUSTOMER INFO
                        if (!o.customerName.isNullOrBlank() || !o.customerPhone.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Customer: ${o.customerName ?: "Walk-in"}",
                                fontWeight = FontWeight.Medium
                            )
                            o.customerPhone?.let {
                                Text(
                                    text = "Phone: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        // 📦 DELIVERY ADDRESS (ONLY IF PRESENT)
                        if (
                            !o.dAddressLine1.isNullOrBlank() ||
                            !o.dCity.isNullOrBlank()
                        ) {
                            Spacer(Modifier.height(6.dp))
                            Text("Delivery Address:", fontWeight = FontWeight.Medium)

                            listOfNotNull(
                                o.dAddressLine1,
                                o.dAddressLine2,
                                listOfNotNull(o.dCity, o.dState, o.dZipcode).joinToString(" ").takeIf { it.isNotBlank() },
                                o.dLandmark?.let { "Near $it" }
                            ).forEach {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }





                    }

                    Spacer(Modifier.height(6.dp))

                    // 🍽 Order type + table
                    Row {
                        Text(
                            "Type: ${o.orderType}",
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        if (!o.tableNo.isNullOrEmpty()) {
                            Text("Table: ${o.tableNo}")
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // 💳 Payment info
                    Text(
                        "Payment: ${o.paymentType} (${o.paymentStatus})",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // 📦 Order status
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

                    // 🕒 Created time (extra clarity)
                    val sdf = rememberDateFormatter()

                    Text(
                        sdf.format(Date(o.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    // 📝 Notes (optional)
                    if (!o.notes.isNullOrEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Notes: ${o.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }


        // 🧾 ITEMS
        Text("Items", style = MaterialTheme.typography.titleMedium)
        Divider(Modifier.padding(vertical = 4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(products, key = { it.id }) { item ->
                OrderProductRow(item)
                Divider(color = Color(0xFFE0E0E0))
            }
        }

        Spacer(Modifier.height(12.dp))

        // 💰 TOTALS
        OrderTotals(subtotal = subtotal, tax = tax, grandTotal = grandTotal)


        Spacer(Modifier.height(12.dp))

        // 🖨 PRINT BUTTON
//        Button(
//            onClick = { viewModel.printOrder() },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Print Order")
//        }
    }
}

@Composable
fun OrderProductRow(item: PosOrderItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            // ✅ VARIANT INDICATOR (SAFE)
            if (item.isVariant && !item.parentId.isNullOrEmpty()) {
                Text(
                    text = "Variant item",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF616161)
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                "${item.quantity} × ₹${"%.2f".format(item.basePrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // ✅ TAX INFO (NO RE-CALCULATION)
            Text(
                text = "Tax: ${item.taxRate}% (${item.taxType})",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "₹${"%.2f".format(item.finalTotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                "₹${"%.2f".format(item.finalPricePerItem)} / item",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun OrderTotals(subtotal: Double, tax: Double, grandTotal: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TotalRow("Items Subtotal", subtotal)
        TotalRow("Total GST", tax)
        Divider(Modifier.padding(vertical = 4.dp))
        TotalRow("Payable Amount", grandTotal, bold = true)
    }
}


@Composable
fun TotalRow(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("₹${"%.2f".format(value)}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}




@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
}