package com.it10x.foodappgstav7_02.ui.bill

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

    val deliveryAddressState = remember {
        mutableStateOf(DeliveryAddressUiState())
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var pendingPaymentType by remember { mutableStateOf<PaymentType?>(null) }

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
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Final Bill", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // ---------------- ITEMS ----------------
        LazyColumn(modifier = Modifier.weight(1f)) {
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

        // ---------------- PAYMENTS ----------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PaymentButton("Cash") {
                onPaymentClick(
                    viewModel,
                    PaymentType.CASH,
                    deliveryAddressState.value,
                    onRequireAddress = {
                        pendingPaymentType = PaymentType.CASH
                        showAddressDialog = true
                    },
                    onProceed = onPayClick
                )
            }

            PaymentButton("Card") {
                onPaymentClick(
                    viewModel,
                    PaymentType.CARD,
                    deliveryAddressState.value,
                    onRequireAddress = {
                        pendingPaymentType = PaymentType.CARD
                        showAddressDialog = true
                    },
                    onProceed = onPayClick
                )
            }

            PaymentButton("UPI") {
                onPaymentClick(
                    viewModel,
                    PaymentType.UPI,
                    deliveryAddressState.value,
                    onRequireAddress = {
                        pendingPaymentType = PaymentType.UPI
                        showAddressDialog = true
                    },
                    onProceed = onPayClick
                )
            }
        }
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
private fun onPaymentClick(
    viewModel: BillViewModel,
    paymentType: PaymentType,
    address: DeliveryAddressUiState,
    onRequireAddress: () -> Unit,
    onProceed: (PaymentType) -> Unit
) {
    if (viewModel.orderTypePublic == "DELIVERY" && !isAddressValid(address)) {
        onRequireAddress()
    } else {
        onProceed(paymentType)
    }
}

// =====================================================
// VALIDATION
// =====================================================
private fun isAddressValid(addr: DeliveryAddressUiState): Boolean {
    return addr.phone.isNotBlank() &&
            addr.line1.isNotBlank() &&
            addr.city.isNotBlank() &&
            addr.zipcode.isNotBlank()
}

// =====================================================
// UI COMPONENTS
// =====================================================
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

                AddressField("City", addressState.value.city) {
                    addressState.value = addressState.value.copy(city = it)
                }

                AddressField("State", addressState.value.state) {
                    addressState.value = addressState.value.copy(state = it)
                }

                AddressField("Zipcode", addressState.value.zipcode) {
                    addressState.value = addressState.value.copy(zipcode = it)
                }

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
