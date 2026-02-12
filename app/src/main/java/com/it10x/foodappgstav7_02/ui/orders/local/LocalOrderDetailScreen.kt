package com.it10x.foodappgstav7_02.ui.orders.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    val discount by viewModel.discount.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ================= HEADER =================
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF616161))
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
                            color = Color.White
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
                        Text(
                            "Sync Status: ${o.syncStatus}",
                            fontWeight = FontWeight.Medium,
                            color = when (o.syncStatus) {
                                "SYNCED" -> Color(0xFF2E7D32)
                                "PENDING" -> Color(0xFFD32F2F)
                                else -> Color.DarkGray
                            }
                        )
                    }

                    // ---------- RIGHT : DELIVERY ADDRESS ----------
                    Column(modifier = Modifier.weight(1f)) {
                        if (o.orderType == "DELIVERY") {
                            Text("Delivery Address", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(o.customerName ?: "Walk-in", fontWeight = FontWeight.Medium)
                            o.customerPhone?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(4.dp))
                            listOfNotNull(
                                o.dAddressLine1,
                                o.dAddressLine2,
                                listOfNotNull(o.dCity, o.dState, o.dZipcode)
                                    .joinToString(" ")
                                    .takeIf { it.isNotBlank() },
                                o.dLandmark?.let { "Near $it" }
                            ).forEach { line ->
                                Text(line, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
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
                .weight(1f)
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
            discount = discount,
            grandTotal = grandTotal,
            onEditClick = { showEditDialog = true }
        )
    }

    // ================= EDIT GRAND TOTAL DIALOG =================
    if (showEditDialog && order != null) {
        EditGrandTotalDialog(
            currentTotal = order!!.grandTotal,
            onDismiss = { showEditDialog = false },
            onConfirm = { newTotal ->
                showEditDialog = false
                viewModel.updateGrandTotal(newTotal)
            }
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("${item.quantity} × ₹${"%.2f".format(item.basePrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (item.isVariant && !item.parentId.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Variant item", style = MaterialTheme.typography.labelSmall, color = Color(0xFF616161))
                }
                Spacer(Modifier.height(2.dp))
                Text("GST ${item.taxRate}% (${item.taxType})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${"%.2f".format(item.finalTotal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("₹${"%.2f".format(item.finalPricePerItem)} / item", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}



@Composable
fun EditGrandTotalDialog(
    currentTotal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var totalText by remember { mutableStateOf(currentTotal.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Grand Total", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) totalText = it },
                    singleLine = true,
                    label = { Text("New Total (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val newTotal = totalText.toDoubleOrNull() ?: currentTotal
                        onConfirm(newTotal)
                    }) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
}


@Composable
fun OrderTotals(
    subtotal: Double,
    tax: Double,
    discount: Double,
    grandTotal: Double,
    onEditClick: () -> Unit = {} // <-- default so old callers still compile
) {
    Column {
        TotalRow("Subtotal", subtotal)
        TotalRow("GST", tax)

        if (discount > 0) {
            TotalRow("Discount", -discount)
        }

        Divider(Modifier.padding(vertical = 4.dp))

        // Grand total with edit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grand Total", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${"%.2f".format(grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onEditClick, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Total", tint = Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
fun TotalRow(label: String, value: Double, bold: Boolean = false) {
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
