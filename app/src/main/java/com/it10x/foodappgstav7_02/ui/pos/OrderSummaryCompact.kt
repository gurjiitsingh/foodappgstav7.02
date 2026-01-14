package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel

@Composable
fun OrderSummaryCompact(
    cartViewModel: CartViewModel
) {
    val items by cartViewModel.cart.collectAsState(initial = emptyList())

    val subTotal = items.sumOf { it.basePrice * it.quantity }

    Column {
        Text("Summary", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        SummaryRow("Subtotal", subTotal)
        SummaryRow("GST", 0.0)
        SummaryRow("Discount", 0.0)

        Divider(Modifier.padding(vertical = 6.dp))

        SummaryRow("Total", subTotal, bold = true)
    }

    @Composable
     fun SummaryRow(
        label: String,
        value: Double,
        bold: Boolean = false
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = if (bold) FontWeight.Bold else null)
            Text("₹$value", fontWeight = if (bold) FontWeight.Bold else null)
        }
    }

}


