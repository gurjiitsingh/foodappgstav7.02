package com.it10x.foodappgstav7_02.ui.bill

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_02.ui.payment.PaymentType
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
// =====================================================
// DELIVERY ADDRESS UI STATE (UI ONLY)
// =====================================================
data class DeliveryAddressUiState(
    val name: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val state: String = "",
    val zipcode: String = "",
    val landmark: String = ""
)

// =====================================================
// BILL SCREEN
// =====================================================
@Composable
fun BillScreen(
    viewModel: BillViewModel,
    onPayClick: (PaymentType) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val deliveryAddressState = remember {
        mutableStateOf(DeliveryAddressUiState())
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var pendingPaymentType by remember { mutableStateOf<PaymentType?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 400.dp) // ensures visible height for tablets
            .padding(16.dp)
    ) {
        // 🔹 Fixed Header




        // 🔹 Scrollable Item List (takes all remaining space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.quantity} x ${item.name}",
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            "$currency%.2f".format(item.itemtotal),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // 🔹 Fixed Footer (Totals)
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(6.dp))

        BillRow("Sub Total", state.subtotal, currency)
        BillRow("Tax", state.tax, currency)

        if (state.discountApplied > 0) {
            BillRow("Discount", -state.discountApplied, currency)
        }

        BillRow("Grand Total", state.total, currency, bold = true)
    }



    // 🔐 Keep ViewModel updated
    viewModel.setDeliveryAddress(deliveryAddressState.value)

    // ---------------- ADDRESS DIALOG ----------------
    if (showAddressDialog) {
        DeliveryAddressDialog(
            addressState = deliveryAddressState,
            onDismiss = { showAddressDialog = false },
            onConfirm = {
                showAddressDialog = false
                pendingPaymentType?.let { onPayClick(it) }
                pendingPaymentType = null
            }
        )
    }
}

// =====================================================
// PAYMENT HANDLER
// =====================================================
//private fun onPaymentClick(
//    viewModel: BillViewModel,
//    paymentType: PaymentType,
//    address: DeliveryAddressUiState,
//    onRequireAddress: () -> Unit,
//    onProceed: (PaymentType) -> Unit
//) {
//    if (viewModel.orderTypePublic == "DELIVERY" && !isAddressValid(address)) {
//        onRequireAddress()
//    } else {
//        onProceed(paymentType)
//    }
//}

private fun onPaymentClick(

    viewModel: BillViewModel,
    context: Context,
    paymentType: PaymentType,
    address: DeliveryAddressUiState,
    onRequireAddress: () -> Unit,
    onProceed: (PaymentType) -> Unit
) {
    if (viewModel.orderTypePublic == "DELIVERY" && !isAddressValid(address)) {
        onRequireAddress()
        return
    }

    viewModel.viewModelScope.launch {

        val hasPending = viewModel.hasPendingKitchenItems()

        if (hasPending) {
            Toast.makeText(
                context,
                "Kitchen items pending. Clear before billing.",
                Toast.LENGTH_LONG
            ).show()
            return@launch
        }

        onProceed(paymentType)
    }
}



// =====================================================
// VALIDATION
// =====================================================
private fun isAddressValid(
    addr: DeliveryAddressUiState,
    requireCity: Boolean = false,
    requireZip: Boolean = false
): Boolean {
    if (addr.phone.isBlank()) return false
    if (addr.line1.isBlank()) return false
    if (requireCity && addr.city.isBlank()) return false
    if (requireZip && addr.zipcode.isBlank()) return false
    return true
}

// =====================================================
// UI COMPONENTS
// =====================================================
@Composable
private fun BillRow(label: String, value: Double,currency: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            "$currency%.2f ".format(value),
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

// =====================================================
// DELIVERY ADDRESS DIALOG (FIXED)
// =====================================================
@Composable
fun DeliveryAddressDialog(
    addressState: MutableState<DeliveryAddressUiState>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text("Delivery Address", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                AddressField("Customer Name", addressState.value.name) {
                    addressState.value = addressState.value.copy(name = it)
                }

                AddressField("Phone", addressState.value.phone) {
                    addressState.value = addressState.value.copy(phone = it)
                }

                AddressField("Address Line 1", addressState.value.line1) {
                    addressState.value = addressState.value.copy(line1 = it)
                }

                AddressField("Address Line 2", addressState.value.line2) {
                    addressState.value = addressState.value.copy(line2 = it)
                }

//                AddressField("City", addressState.value.city) {
//                    addressState.value = addressState.value.copy(city = it)
//                }

//                AddressField("State", addressState.value.state) {
//                    addressState.value = addressState.value.copy(state = it)
//                }

//                AddressField("Zipcode", addressState.value.zipcode) {
//                    addressState.value = addressState.value.copy(zipcode = it)
//                }

                AddressField("Landmark", addressState.value.landmark) {
                    addressState.value = addressState.value.copy(landmark = it)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isAddressValid(addressState.value)) {
                                onConfirm()
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
