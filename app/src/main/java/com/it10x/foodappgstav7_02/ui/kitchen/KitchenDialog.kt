package com.it10x.foodappgstav7_02.ui.kitchen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun KitchenDialog(
    tableNo: String,
    viewModel: KitchenViewModel,
    orderType: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Kitchen – Table $tableNo") },
        text = {
            KitchenScreen(
                tableNo = tableNo,
                viewModel = viewModel,
                onKitchenEmpty = onDismiss,
                orderType = orderType
            )
        }
    )
}
