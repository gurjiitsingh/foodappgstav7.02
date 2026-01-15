package com.it10x.foodappgstav7_02.ui.bill

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.it10x.foodappgstav7_02.ui.payment.PaymentType

@Composable
fun BillScreenDialog(
    tableId: String,
    tableViewModel: com.it10x.foodappgstav7_02.viewmodel.TableViewModel,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // ✅ ViewModel per TABLE (stable, correct)
    val viewModel: BillViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "BillViewModel_$tableId",   // ⭐ IMPORTANT FIX
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
            BillScreen(
                viewModel = viewModel,
                onPayClick = { paymentType ->
                    viewModel.payBill(paymentType.name)

                    if (!tableId.startsWith("TAKEAWAY") && !tableId.startsWith("DELIVERY")) {
                        // ✅ ONLY for dine-in
                        tableViewModel.releaseTable(tableId)
                    }

                    onClose()
                }
            )
        }
    )
}
