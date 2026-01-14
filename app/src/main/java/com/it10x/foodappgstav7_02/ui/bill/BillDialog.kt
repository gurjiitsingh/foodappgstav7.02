package com.it10x.foodappgstav7_02.ui.bill

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.it10x.foodappgstav7_02.ui.payment.PaymentType

@Composable
fun BillScreenDialog(
    tableId: String,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // ✅ ViewModel CREATED ONCE (STABLE)
    val viewModel: BillViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = BillViewModelFactory(
            application = context.applicationContext as android.app.Application,
            tableId = tableId
        )
    )

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        },
        title = {
            Text(text = "Final Bill")
        },
        text = {
            // ✅ PASS VIEWMODEL — DO NOT CREATE INSIDE
            BillScreen(
                viewModel = viewModel,
                onPayClick = { paymentType ->
                    viewModel.payBill(paymentType.name)
                    onClose()
                }
            )
        }
    )
}
