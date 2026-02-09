package com.it10x.foodappgstav7_02.ui.bill

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_02.ui.bill.BillViewModel
import com.it10x.foodappgstav7_02.ui.bill.BillViewModelFactory

@Composable
fun BillDialog(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    selectedTableName: String
) {
    if (!showBill || sessionId == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Column(
                    modifier = Modifier
                        .weight(2.2f)
                        .padding(8.dp)
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val billViewModel: BillViewModel = viewModel(
                        key = "BillVM_${sessionId}",
                        factory = BillViewModelFactory(
                            application = LocalContext.current.applicationContext as Application,
                            tableId = tableId ?: orderType,
                            tableName = selectedTableName,
                            orderType = orderType
                        )
                    )

                    Text(
                        "Final Bill",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.4f))

                    BillScreen(
                        viewModel = billViewModel,
                        onPayClick = { paymentType ->
                            billViewModel.payBill(paymentType.name)
                            onDismiss()
                        }
                    )
                }

                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val billViewModel: BillViewModel = viewModel(
                        key = "BillVM_${sessionId}",
                        factory = BillViewModelFactory(
                            application = LocalContext.current.applicationContext as Application,
                            tableId = tableId ?: orderType,
                            tableName = selectedTableName,
                            orderType = orderType
                        )
                    )

                    val discountFlat = remember { mutableStateOf("") }
                    val discountPercent = remember { mutableStateOf("") }
                    var activeField by remember { mutableStateOf("FLAT") }



                    // ---------------- DISCOUNT SECTION ----------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Actions",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )

                        // ✅ Compact Close button (top-right)
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(28.dp)
                                .width(70.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C), // POS red
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }


                    Text("Discount", style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // FLAT BOX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (activeField == "FLAT") Color(0xFF424242) else Color(0xFF616161),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (activeField == "FLAT") Color(0xFF90CAF9) else Color(0xFFBDBDBD),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeField = "FLAT"
                                    discountPercent.value = "" // ✅ clear other field
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = discountFlat.value.ifEmpty { "Flat" },
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // PERCENT BOX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (activeField == "PERCENT") Color(0xFF424242) else Color(0xFF616161),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (activeField == "PERCENT") Color(0xFF90CAF9) else Color(0xFFBDBDBD),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeField = "PERCENT"
                                    discountFlat.value = "" // ✅ clear other field
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = discountPercent.value.ifEmpty { "%" },
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }





                    // ---------- CUSTOM NUM PAD ----------
                    val buttons = listOf(
                        listOf("1", "2", "3", "4", "5", "6"),
                        listOf("7", "8", "9", "0", ".", "←")
                    )

                    buttons.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            row.forEach { label ->
                                Button(
                                    onClick = {
                                        when (label) {
                                            "←" -> {
                                                if (activeField == "FLAT" && discountFlat.value.isNotEmpty()) {
                                                    discountFlat.value = discountFlat.value.dropLast(1)
                                                    billViewModel.setFlatDiscount(discountFlat.value.toDoubleOrNull() ?: 0.0)
                                                } else if (activeField == "PERCENT" && discountPercent.value.isNotEmpty()) {
                                                    discountPercent.value = discountPercent.value.dropLast(1)
                                                    billViewModel.setPercentDiscount(discountPercent.value.toDoubleOrNull() ?: 0.0)
                                                }
                                            }

                                            else -> {
                                                if (activeField == "FLAT") {
                                                    discountFlat.value += label
                                                    billViewModel.setFlatDiscount(discountFlat.value.toDoubleOrNull() ?: 0.0)
                                                    discountPercent.value = ""
                                                } else {
                                                    discountPercent.value += label
                                                    billViewModel.setPercentDiscount(discountPercent.value.toDoubleOrNull() ?: 0.0)
                                                    discountFlat.value = ""
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    contentPadding = PaddingValues(vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE0E0E0), // soft gray
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(label, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 4.dp))

                    // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------
                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 💵 CASH + 💳 CARD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { billViewModel.payBill("CASH"); onDismiss() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),      // POS green
                                    contentColor = Color.White
                                )
                            ) { Text("💵 Cash", fontSize = 13.sp) }

                            Button(
                                onClick = { billViewModel.payBill("CARD"); onDismiss() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2),      // POS blue
                                    contentColor = Color.White
                                )
                            ) { Text("💳 Card", fontSize = 13.sp) }
                        }

                        // 📱 UPI + 💰 WALLET
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { billViewModel.payBill("UPI"); onDismiss() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800),      // POS orange
                                    contentColor = Color.White
                                )
                            ) { Text("📱 UPI", fontSize = 13.sp) }

                            Button(
                                onClick = { billViewModel.payBill("WALLET"); onDismiss() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0),      // POS purple
                                    contentColor = Color.White
                                )
                            ) { Text("💰 Wallet", fontSize = 13.sp) }
                        }
                    }

                }

            }
        }
    }
}
