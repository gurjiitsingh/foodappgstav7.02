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
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import com.it10x.foodappgstav7_02.data.pos.viewmodel.POSOrdersViewModel

import android.provider.Settings
import android.os.Build
import android.util.Log
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_02.BuildConfig
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.ui.bill.BillViewModel
import com.it10x.foodappgstav7_02.ui.bill.BillViewModelFactory
import com.it10x.foodappgstav7_02.ui.kitchen.KitchenViewModel
import com.it10x.foodappgstav7_02.ui.kitchen.KitchenViewModelFactory
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
    cartViewModel: CartViewModel,
    ordersViewModel: POSOrdersViewModel,
    tableViewModel: PosTableViewModel,
    orderType: String,
    tableNo: String?,
    paymentType: String,
    onPaymentChange: (String) -> Unit,
    onOrderPlaced: () -> Unit,
    onOpenKitchen: (String) -> Unit,
    onOpenBill: (String) -> Unit,
    isMobile: Boolean,
    repository: POSOrdersRepository,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current



    val application = context.applicationContext as android.app.Application
    val db = AppDatabaseProvider.get(application)

    val printerManager = PrinterManager(context)


    val billViewModel: BillViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "BillVM_${tableNo ?: orderType}",
        factory = BillViewModelFactory(
            application = application,
            tableId = tableNo ?: orderType,
            orderType = orderType,

        )
    )

    val kitchenViewModel: KitchenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "KitchenVM_${tableNo ?: orderType}",
        factory = KitchenViewModelFactory(
            application,
            tableId = tableNo ?: orderType,
            orderType = orderType,
            repository = repository
        )
    )




    val cartItems: List<PosCartEntity> by
    cartViewModel.cart.collectAsState(initial = emptyList())

    // ---------------- TABLE STATE ----------------
    val tables by tableViewModel.tables.collectAsState()
    val currentTable = tables.find { it.table.id == tableNo }
    val tableStatus = currentTable?.table?.status ?: "AVAILABLE"

    val isDineIn = orderType == "DINE_IN"
    val isRunning = tableStatus == "OCCUPIED"
    val isBillRequested = tableStatus == "BILL_REQUESTED"

    // ---------------- POS DERIVED STATE ----------------
    val hasItems = cartItems.isNotEmpty()
    val hasTable = isDineIn && tableNo != null

    val canSendToKitchen =
        hasItems && (!isDineIn || hasTable)

    val canRequestBill =
        isDineIn && isRunning && cartItems.isEmpty()

    val canOpenBill =
        when (orderType) {
            "DINE_IN" -> isBillRequested
            "TAKEAWAY", "DELIVERY" -> true
            else -> false
        }

    val canOpenKitchen =
        when (orderType) {
            "DINE_IN" -> hasTable && (isRunning || isBillRequested)
            "TAKEAWAY", "DELIVERY" -> true
            else -> false
        }





    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isMobile) {
                    Modifier.fillMaxHeight(0.88f)   // ✅ mobile bottom sheet height
                } else {
                    Modifier.widthIn(max = 320.dp).fillMaxHeight()
                }
            )
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

//        Column(modifier = Modifier.padding(bottom = 12.dp)) {
//            Text(
//                text = "Order Type: $prettyOrderType",
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.SemiBold
//            )
//            Text(
//                text = if (hasTable) "Table: $tableNo" else "Table: —",
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray
//            )
//        }

//        Divider()

        if (isMobile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = { onClose?.invoke() }
                ) {
                    Text("Close")
                }
            }
            Divider()
        }


        // ---------- CART ----------
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp)
        ) {
            items(cartItems, key = { it.productId }) { item ->
                CartRow(item, cartViewModel)
            }
        }

        Divider()

        OrderSummaryScreen(cartViewModel)

        // =========================================================
        // =================== POS ACTION BUTTONS ==================
        // =========================================================

        // ---------- SEND TO KITCHEN ----------
        if (canSendToKitchen) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                onClick = {
                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )

                    kitchenViewModel.sendToKitchen(
                        orderType = orderType,
                        tableNo = tableNo,
                        sessionId = cartViewModel.sessionKey.value!!,
                        paymentType = "UNPAID",
                        deviceId = deviceId,
                        deviceName = Build.MODEL ?: "Unknown Device",
                        appVersion = BuildConfig.VERSION_NAME
                    )

// ✅ OCCUPY TABLE ONLY WHEN FIRST ORDER IS SENT
                    if (orderType == "DINE_IN" && tableNo != null) {
                        tableViewModel.occupyTable(tableNo)
                    }
                    onOrderPlaced()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    contentColor = Color.White
                )
            ) {
                Text("Send to Kitchen")
            }
        }

        // ---------- REQUEST BILL ----------
        if (canRequestBill) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    tableViewModel.requestBill(tableNo!!)
                    onOrderPlaced()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFACC15),
                    contentColor = Color.Black
                )
            ) {
                Text("Request Bill")
            }
        }

        // ---------- OPEN BILL ----------
        if (canOpenBill) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    tableNo?.let { onOpenBill(it) }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text("Open Bill")
            }
        }

        // ---------- OPEN KITCHEN ----------
        if (canOpenKitchen) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
//                    tableNo?.let { onOpenKitchen(it) }
                    onOpenKitchen(tableNo ?: orderType)
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












