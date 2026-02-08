package com.it10x.foodappgstav7_02.ui.bill

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.ui.payment.PaymentType
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel

@Composable
fun BillScreenDialog(
    sessionId: String,
    tableId: String,
    tableName: String,
    orderType: String,
    tableViewModel: PosTableViewModel,
    onClose: () -> Unit,

) {
    val context = LocalContext.current

    val billingTitle = when (orderType) {
        "DINE_IN" -> "Table ${tableId ?: ""}"
        "TAKEAWAY" -> "Takeaway"
        "DELIVERY" -> "Delivery"
        else -> sessionId
    }

    // ✅ Initialize dependencies
    val application = context.applicationContext as android.app.Application
    val db = AppDatabaseProvider.get(application)
//    val repository = POSOrdersRepository(db)
    val printerManager = PrinterManager(context)

    // ✅ Create ViewModel (stable per table)
    val viewModel: BillViewModel = viewModel(
        key = "BillViewModel_$sessionId",   // ensures unique VM per table
        factory = BillViewModelFactory(
            application = application,
            tableId = tableId,
            tableName = tableName,
            orderType = orderType,

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
        title = { Text(text = "Final Bill: $tableName") },
        text = {
            BillScreen(
                viewModel = viewModel,
                onPayClick = { paymentType ->
                    viewModel.payBill(paymentType.name)

                    // ✅ Release table after billing (Dine-in only)
//                    if (!tableId.startsWith("TAKEAWAY") && !tableId.startsWith("DELIVERY")) {
//                        tableViewModel.releaseTable(tableId)
//                    }

                    onClose()
                },
            )
        }
    )
}
