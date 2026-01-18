package com.it10x.foodappgstav7_02.ui.sales

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onBack: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            // ---------- DATE RANGE BUTTONS ----------
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    val from = viewModel.startOfToday()
                    val to = viewModel.endOfToday()
                    viewModel.setDateRange(from, to)
                }) {
                    Text("Today")
                }

                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val from = calendar.timeInMillis
                    val to = viewModel.endOfToday()
                    viewModel.setDateRange(from, to)
                }) {
                    Text("This Month")
                }

                Button(onClick = {
                    // Custom Date Picker
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(context,
                        { _, year, month, dayOfMonth ->
                            val fromCal = Calendar.getInstance()
                            fromCal.set(year, month, dayOfMonth, 0, 0, 0)
                            fromCal.set(Calendar.MILLISECOND, 0)
                            val from = fromCal.timeInMillis

                            // Ask user for 'to' date next
                            DatePickerDialog(context,
                                { _, toYear, toMonth, toDay ->
                                    val toCal = Calendar.getInstance()
                                    toCal.set(toYear, toMonth, toDay, 23, 59, 59)
                                    toCal.set(Calendar.MILLISECOND, 999)
                                    val to = toCal.timeInMillis
                                    viewModel.setDateRange(from, to)
                                }, year, month, dayOfMonth
                            ).show()

                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text("Custom")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- LOADING ----------
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // ---------- SUMMARY ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Total Sales", uiState.totalSales)
                    SummaryRow("Tax", uiState.taxTotal)
                    SummaryRow("Discount", uiState.discountTotal)
                    Spacer(Modifier.height(8.dp))
                    uiState.paymentBreakup.forEach { (type, amount) ->
                        SummaryRow(type, amount)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- SALES LIST ----------
            Text("Orders", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.orders) { order ->
                    SalesOrderRow(order)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("₹ %.2f".format(value))
    }
}

@Composable
private fun SalesOrderRow(order: PosOrderMasterEntity) {

    val time = remember(order.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${order.srno}")
                Text("₹ %.2f".format(order.grandTotal))
            }
            Spacer(Modifier.height(4.dp))
            Text("${order.orderType} • ${order.paymentType}",
                style = MaterialTheme.typography.bodySmall)
            Text(time, style = MaterialTheme.typography.bodySmall)
        }
    }
}
