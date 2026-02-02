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
import androidx.compose.foundation.BorderStroke
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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
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


    val sessionId = cartViewModel.sessionKey.collectAsState().value ?: return

    val kitchenViewModel: KitchenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "KitchenVM_$sessionId",
        factory = KitchenViewModelFactory(
            application,
            tableId = tableNo ?: orderType,
            sessionId = sessionId,
            orderType = orderType,
            repository = repository,
            cartViewModel = cartViewModel
        )
    )

    val billViewModel: BillViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "BillVM_${tableNo ?: orderType}",
        factory = BillViewModelFactory(
            application = application,
            tableId = tableNo ?: orderType,
            orderType = orderType,

            )
    )

    //val orderRef = if (orderType == "DINE_IN") tableNo ?: "" else cartViewModel.sessionKey.value ?: ""
    val orderRef = if (orderType == "DINE_IN") tableNo ?: "" else orderType

    val kitchenItems by kitchenViewModel
        .getPendingItems(orderRef = orderRef, orderType = orderType)
        .collectAsState(initial = null)

    val BillItems by billViewModel
        .getDoneItems(orderRef = orderRef, orderType = orderType)
        .collectAsState(initial = null)


    val hasKitchenItems = kitchenItems?.isNotEmpty() == true

    val hasBillItems = BillItems?.isNotEmpty() == true


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

//    val canOpenBill =
//        hasBillItems && when (orderType) {
//            "DINE_IN" -> isBillRequested
//            "TAKEAWAY", "DELIVERY" -> true
//            else -> false
//        }
    val canOpenBill =
        hasBillItems && when (orderType) {
            "DINE_IN" -> true
            "TAKEAWAY", "DELIVERY" -> true
            else -> false
        }


    val canOpenKitchen =
        hasKitchenItems && when (orderType) {
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
        //    .background(Color(0xFFF7F7F7))
            .padding(12.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {

        // ---------- ORDER INFO ----------


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
                CartRow(
                    item = item,
                    cartViewModel = cartViewModel,
                    onBillAction = { cartItem, print ->

                        kitchenViewModel.sendSingleItemDirectlyToBill(
                            cart = cartItem,
                            orderType = orderType,
                            tableNo = tableNo,
                            sessionId = sessionId,
                            print = print
                        )

                        // OPTIONAL (recommended later)
                        // cartViewModel.decrease(cartItem.productId)
                    }
                )
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

                    // kitchenViewModel.deleteAllKotItems();
                    kitchenViewModel.logAllKotItems()

                    kitchenViewModel.sendToKitchen(
                        orderType = orderType,
                        tableNo = tableNo,
                        sessionId = sessionId,
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

//        Button(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 12.dp),
//            onClick = {
//
//              //  kitchenViewModel.deleteAllKotItems()
//                kitchenViewModel.logAllKotItems()
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF16A34A),
//                contentColor = Color.White
//            )
//        ) {
//            Text("Show all items")
//        }


        // ---------- REQUEST BILL ----------
//        if (canRequestBill) {
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp),
//                onClick = {
//                    tableViewModel.requestBill(tableNo!!)
//                    onOrderPlaced()
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFFACC15),
//                    contentColor = Color.Black
//                )
//            ) {
//                Text("Request Bill")
//            }
//        }

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
    cartViewModel: CartViewModel,
    onBillAction: (item: PosCartEntity, print: Boolean) -> Unit
) {
    val DarkGray = Color(0xFF111827)  // near-black POS background
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(modifier = Modifier.padding(10.dp)) {

            // ================= TOP ROW (UNCHANGED) =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ---------- ITEM INFO ----------
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "₹${item.basePrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                // ---------- QUANTITY CONTROLS ----------
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = { cartViewModel.decrease(item.productId) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFFDC2626),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Text(
                            text = "−",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { cartViewModel.addToCart(item) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFF16A34A),
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

// ================= BILL ACTION ROW =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // 🔶 Bill + KOT (PRIMARY)
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onBillAction(item, true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Bill +",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Kitchen",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print",
                        modifier = Modifier.size(16.dp)
                    )


                }

                // 🔶 Bill Only (SECONDARY / OUTLINED)
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onBillAction(item, false)
                    },
                    border = BorderStroke(1.5.dp, Color(0xFFF97316)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF97316)
                    )
                ) {
//                    Icon(
//                        imageVector = Icons.Default.Restaurant,
//                        contentDescription = "Kitchen",
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(modifier = Modifier.width(6.dp))
                   Text(
                        "Bill",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }

        }
    }
}













