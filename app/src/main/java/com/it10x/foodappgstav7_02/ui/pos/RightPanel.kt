package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import com.it10x.foodappgstav7_02.data.local.viewmodel.POSOrdersViewModel

import android.provider.Settings
import android.os.Build
import android.util.Log
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_02.BuildConfig
import com.it10x.foodappgstav7_02.viewmodel.TableViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
    cartViewModel: CartViewModel,
    ordersViewModel: POSOrdersViewModel,
    tableViewModel: TableViewModel,
    orderType: String,
    tableNo: String?,
    paymentType: String,
    onPaymentChange: (String) -> Unit,
    onOrderPlaced: () -> Unit,
    onOpenKitchen: (String) -> Unit,
    onOpenBill: (String) -> Unit   // ✅ ADD THIS
){
    val context = LocalContext.current

    val cartItems: List<PosCartEntity> by
    cartViewModel.cart.collectAsState(initial = emptyList())

    // ---------------- TABLE STATE ----------------
    val tables by tableViewModel.tables.collectAsState()
    val currentTable = tables.find { it.table.id == tableNo }
    val tableStatus = currentTable?.table?.status ?: "AVAILABLE"

    val isDineIn = orderType == "DINE_IN"
    val isRunning = tableStatus == "OCCUPIED"
    val isBillRequested = tableStatus == "BILL_REQUESTED"

    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight()
            .background(Color(0xFFF7F7F7))
            .padding(12.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {

        // ---------- ORDER INFO ----------
        val prettyOrderType = when (orderType) {
            "DINE_IN" -> "Dine In"
            "DELIVERY" -> "Delivery"
            "TAKEAWAY" -> "Takeaway"
            else -> orderType
        }

        val showTable = isDineIn && tableNo != null

        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "Order Type: $prettyOrderType",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
            Text(
                text = if (showTable) "Table: $tableNo" else "Table: —",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }

        Divider(Modifier.padding(vertical = 8.dp))

        // ---------- CART ITEMS ----------
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cartItems, key = { it.productId }) { item ->
                CartRow(item, cartViewModel)
            }
        }

        Divider(Modifier.padding(vertical = 8.dp))

        Text(
            text = "Payment",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OrderSummaryScreen(cartViewModel)

        // ---------- SEND TO KITCHEN ----------
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            onClick = {
                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                ordersViewModel.placeOrder(
                    orderType = orderType,
                    tableNo = tableNo,
                    paymentType = "UNPAID",
                    deviceId = deviceId,
                    deviceName = Build.MODEL ?: "Unknown Device",
                    appVersion = BuildConfig.VERSION_NAME
                )

                cartViewModel.clear()
                tableViewModel.loadTables() // refresh table UI
                onOrderPlaced()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White
            )
        ) {
            Text("Send to Kitchen")
        }

        // ---------- REQUEST BILL ----------
        if (isDineIn && isRunning && cartItems.isEmpty()) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    tableViewModel.requestBill(tableNo!!) // reactive update
                    onOrderPlaced() // refresh UI if needed
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFACC15),
                    contentColor = Color.Black
                )
            ) {
                Text("Request Bill")
            }
        }

        // ---------- CLOSE TABLE ----------
        // ---------- OPEN BILL ----------
        if (isDineIn && isBillRequested) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    tableNo?.let { onOpenBill(it) }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB), // 🔵 blue
                    contentColor = Color.White
                )
            ) {
                Text("Open Bill")
            }
        }


//        if (isDineIn && isBillRequested) {
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp),
//                onClick = {
//                    tableViewModel.closeTable(tableNo!!) // closes orders + marks AVAILABLE
//                    onOrderPlaced()
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFDC2626),
//                    contentColor = Color.White
//                )
//            ) {
//                Text("Close Table")
//            }
//
//        }


        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = {
                tableNo?.let { onOpenKitchen(it) }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White
            )
        ) {
            Text("Open Kitchen")
        }

    }
}







@Composable
fun CartRow(
    item: PosCartEntity,
    cartViewModel: CartViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White   // ✅ WHITE CARD
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ---------- ITEM INFO ----------
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${item.basePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // ---------- QUANTITY CONTROLS ----------
// ---------- QUANTITY CONTROLS ----------
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖ MINUS BUTTON
                IconButton(
                    onClick = { cartViewModel.decrease(item.productId) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFDC2626), // 🔴 red-600
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Text(
                        text = "−",
                        color = Color.White,
                        fontSize = 20.sp,              // ⬆ bigger
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                // ➕ PLUS BUTTON
                IconButton(
                    onClick = { cartViewModel.addToCart(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFF16A34A), // 🟢 green-600
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = 20.sp,              // ⬆ bigger
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}












