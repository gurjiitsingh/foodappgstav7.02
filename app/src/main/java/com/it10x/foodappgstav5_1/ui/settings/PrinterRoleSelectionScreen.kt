package com.it10x.foodappgstav5_1.com.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.PrinterPreferences
import com.it10x.foodappgstav5_1.data.PrinterRole

@Composable
fun PrinterRoleSelectionScreen(
    prefs: PrinterPreferences,
    onBillingClick: () -> Unit,
    onKitchenClick: () -> Unit
) {
    val billingPrinter = prefs.getPrinterType(PrinterRole.BILLING)
    val kitchenPrinter = prefs.getPrinterType(PrinterRole.KITCHEN)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Select Printer",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        // -------- Billing --------
        PrinterRoleCard(
            title = "Billing Printer",
            selected = billingPrinter?.name ?: "Not configured",
            onClick = onBillingClick
        )

        Spacer(Modifier.height(20.dp))

        // -------- Kitchen --------
        PrinterRoleCard(
            title = "Kitchen Printer",
            selected = kitchenPrinter?.name ?: "Not configured",
            onClick = onKitchenClick
        )
    }
}

@Composable
private fun PrinterRoleCard(
    title: String,
    selected: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Selected: $selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
