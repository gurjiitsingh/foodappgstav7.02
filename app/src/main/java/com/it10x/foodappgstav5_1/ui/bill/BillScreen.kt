package com.it10x.foodappgstav5_1.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.ui.payment.PaymentType




import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.unit.dp

@Composable
fun BillScreen(
    viewModel: BillViewModel,
    onPayClick: (PaymentType) -> Unit
) {
    val state by viewModel.uiState.collectAsState(
        initial = BillUiState()  // 🔹 provide initial value
    )



    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Final Bill", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(state.items) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity} x ${item.name}")
                    Text("₹%.2f".format(item.finalTotal))
                }
            }
        }

        Divider()

        BillRow("Sub Total", state.subtotal)
        BillRow("Tax", state.tax)
        BillRow("Grand Total", state.total, bold = true)

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PaymentButton("Cash") { onPayClick(PaymentType.CASH) }
            PaymentButton("Card") { onPayClick(PaymentType.CARD) }
            PaymentButton("UPI") { onPayClick(PaymentType.UPI) }
        }
    }
}

@Composable
private fun BillRow(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            "₹%.2f".format(value),
            fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
    }
}

@Composable
private fun PaymentButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text)
    }
}
