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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SoupKitchen
import com.it10x.foodappgstav7_02.ui.cart.CartRow
import com.it10x.foodappgstav7_02.ui.theme.PosError
import com.it10x.foodappgstav7_02.ui.theme.PosSuccess
import com.it10x.foodappgstav7_02.ui.theme.PosWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
    cartViewModel: CartViewModel,
    ordersViewModel: POSOrdersViewModel,
    tableViewModel: PosTableViewModel,
    orderType: String,
    tableNo: String,
    tableName: String,
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
            tableName = tableName,
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
            tableName = tableName,
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

    val canOpenKitchen = hasKitchenItems
  //  val canOpenBill = hasBillItems

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
                    tableNo = tableNo,
                    onCartActionDirectMoveToBill = { cartItem, print ->

                        kitchenViewModel.sendSingleItemDirectlyToBill_Print_noPrint(
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp), // ⬅️ reduced top padding
            verticalArrangement = Arrangement.spacedBy(6.dp) // ⬅️ tighter spacing between rows
        ) {

            // 🔹 Row 1 — SEND TO KITCHEN (half width, left aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    modifier = Modifier
                        .weight(0.5f) // ⬅️ half width
                        .padding(start = 4.dp, end = 4.dp), // ⬅️ compact outer padding
                    enabled = canSendToKitchen,
                    onClick = {
                        if (!canSendToKitchen) return@Button

                        val deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )

                        kitchenViewModel.sendToKitchenMainButton(
                            orderType = orderType,
                            tableNo = tableNo,
                            sessionId = sessionId,
                            paymentType = "UNPAID",
                            deviceId = deviceId,
                            deviceName = Build.MODEL ?: "Unknown Device",
                            appVersion = BuildConfig.VERSION_NAME
                        )

                        if (orderType == "DINE_IN" && tableNo != null) {
                            tableViewModel.occupyTable(tableNo)
                        }

                        onOrderPlaced()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp), // ⬅️ reduced inner padding
                    colors = ButtonDefaults.buttonColors(containerColor = PosSuccess)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // 🍳 Pan Icon (use LocalDining or SoupKitchen based on available icons)
                        Text(
                            text = "Send to Kichen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.SoupKitchen, // ⬅️ change to LocalDining if you prefer
                            contentDescription = "Send to Kitchen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // 🔹 Row 2 — OPEN KITCHEN + BILL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // OPEN KITCHEN VIEW
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canOpenKitchen,
                    onClick = {
                        if (!canOpenKitchen) return@Button
                        onOpenKitchen(tableNo ?: orderType)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PosSuccess)
                ) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = "Open Kitchen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // OPEN BILL
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canOpenBill,
                    onClick = {
                        if (!canOpenBill) return@Button
                        tableNo?.let { onOpenBill(it) }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PosWarning,
                        contentColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Bill",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }





    }
}






















