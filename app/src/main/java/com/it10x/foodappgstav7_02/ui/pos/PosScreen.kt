package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_02.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import com.it10x.foodappgstav7_02.data.pos.viewmodel.getParentProducts
import com.it10x.foodappgstav7_02.data.pos.viewmodel.POSOrdersViewModel


import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_02.data.pos.entities.TableEntity
import com.it10x.foodappgstav7_02.ui.cart.CartViewModelFactory
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_02.ui.kitchen.KitchenScreen
import com.it10x.foodappgstav7_02.ui.bill.BillScreenDialog

import com.it10x.foodappgstav7_02.ui.kitchen.KitchenViewModel
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import com.it10x.foodappgstav7_02.ui.cart.CartUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    onOpenSettings: () -> Unit,
    ordersViewModel: POSOrdersViewModel,
    posSessionViewModel: PosSessionViewModel
) {
    var showTableSelector by rememberSaveable() {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    val tableId by posSessionViewModel.tableId.collectAsState()
    var orderType by remember { mutableStateOf("DINE_IN") }


    LaunchedEffect(Unit) {
        cartViewModel.uiEvent.collect { event ->
            when (event) {

                CartUiEvent.SessionRequired -> {
                    if (orderType == "DINE_IN") {
                        showTableSelector = true
                        Toast.makeText(
                            context,
                            "Select table to continue Dine-In order",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Order session not ready. Please retry.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                CartUiEvent.TableRequired -> {
                    showTableSelector = true
                    Toast.makeText(
                        context,
                        "Please select a table first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    val tableName by posSessionViewModel.tableName.collectAsState()

    val categories by db.categoryDao().getAll().collectAsState(initial = emptyList())

    val allProducts by db.productDao().getAll().collectAsState(initial = emptyList())
    val parentProducts = remember(allProducts) {
        getParentProducts(allProducts)
    }

    var selectedCatId by remember { mutableStateOf<String?>(null) }



    val tableVm: PosTableViewModel = viewModel()
    val tables by tableVm.tables.collectAsState()
    LaunchedEffect(Unit) { tableVm.loadTables() }


    val filteredProducts = remember(parentProducts, selectedCatId) {
        if (selectedCatId == null) {
            emptyList()
        } else {
            parentProducts.filter { it.categoryId == selectedCatId }
        }
    }

    val variants = remember(allProducts) {
        allProducts.filter {
            it.type == "variant"
        }
    }

    val variantsMap = remember(allProducts) {
        allProducts
            .filter { it.type == "variant" && it.parentId != null }
            .groupBy { it.parentId }
    }






    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.quantity }

    var showCartSheet by remember { mutableStateOf(false) }



    //var showTableSelector by remember { mutableStateOf(false) }
    // ✅ PAYMENT TYPE STATE (DEFAULT CASH)
    var paymentType by remember { mutableStateOf("CASH") }

    // ✅ NEW: POPUP STATES
    var showKitchen by remember { mutableStateOf(false) }
    var showBill by remember { mutableStateOf(false) }



    LaunchedEffect(orderType, tableId) {
        if (orderType == "DINE_IN" && !tableId.isNullOrBlank()) {
            cartViewModel.initSession("DINE_IN", tableId)
        } else {
            cartViewModel.initSession(orderType)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PosBackground) // ✅ ONLY COLOR
    ) {

        Row(modifier = Modifier.fillMaxSize()) {

            // ---------- LEFT CATEGORY SIDEBAR ----------
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(PosSidebarBackground)
                    .padding(15.dp)   // ✅ SAME AS PRODUCTS
            ) {



                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(categories) { c ->
                        CategoryButton(
                            label = c.name,
                            selected = selectedCatId == c.id
                        ) {
                            selectedCatId = c.id
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }



            // ---------- PRODUCTS ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {

                // ---------- ORDER CONTROLS ----------
                if (isPhone) {
                    // ===== MOBILE: 2 ROWS =====
                    // Row 1: Dine In + Takeaway
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PosOrderTypeButton(
                            label = "Dine In",
                            selected = orderType == "DINE_IN",
                            onClick = {
                                orderType = "DINE_IN"
                                showTableSelector = true
                                cartViewModel.initSession(orderType, tableId)
                            }
                        )
                        PosOrderTypeButton(
                            label = "Takeaway",
                            selected = orderType == "TAKEAWAY",
                            onClick = {
                                orderType = "TAKEAWAY"
                                posSessionViewModel.clearTable()
                                showTableSelector = false
                                cartViewModel.initSession("TAKEAWAY")
                            }
                        )
                    }

                    // Row 2: Delivery + Table Chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PosOrderTypeButton(
                            label = "Delivery",
                            selected = orderType == "DELIVERY",
                            onClick = {
                                orderType = "DELIVERY"
                                posSessionViewModel.clearTable()
                                showTableSelector = false
                                cartViewModel.initSession("DELIVERY")
                            }
                        )

                        if (orderType == "DINE_IN" && tableName != null) {
                            OrderChip(
                                label = tableName!!,
                                selected = true,
                                onClick = { showTableSelector = true }
                            )
                        }
                    }
                }
                // ===== TABLET: SINGLE ROW =====
                if (!isPhone) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PosOrderTypeButton(
                        label = "Dine In",
                        selected = orderType == "DINE_IN",
                        onClick = {
                            orderType = "DINE_IN"
                            showTableSelector = true
                            cartViewModel.initSession(orderType, tableId)
                        }
                    )

                    PosOrderTypeButton(
                        label = "Takeaway",
                        selected = orderType == "TAKEAWAY",
                        onClick = {
                            orderType = "TAKEAWAY"
                            posSessionViewModel.clearTable()
                            showTableSelector = false
                            cartViewModel.initSession("TAKEAWAY")
                        }
                    )

                    PosOrderTypeButton(
                        label = "Delivery",
                        selected = orderType == "DELIVERY",
                        onClick = {
                            orderType = "DELIVERY"
                            posSessionViewModel.clearTable()
                            showTableSelector = false
                            cartViewModel.initSession("DELIVERY")
                        }
                    )

                    if (orderType == "DINE_IN" && tableName != null) {
                        OrderChip(
                            label = tableName!!,
                            selected = true,
                            onClick = { showTableSelector = true }
                        )
                    }
                }

            }


                ProductList(
                    filteredProducts = filteredProducts,
                    variants = variants,
                    cartViewModel = cartViewModel,
                    tableNo = tableId,  // fallback if null
                    posSessionViewModel = posSessionViewModel  // 🔑 pass it
                )




                if (showTableSelector && orderType == "DINE_IN") {
                    TableSelectorGrid(
                        tables = tables, // ✅ use dynamic list
                        selectedTable = tableId,


                        onTableSelected = { tableId ->
                            val table = tables.first { it.table.id == tableId }.table
                            posSessionViewModel.setTable(
                                tableId = table.id,
                                tableName = table.tableName
                            )
                            // 🔹 Init DINE_IN session
                            cartViewModel.initSession("DINE_IN", table.id)
                            showTableSelector = false
                        },


                        onDismiss = { showTableSelector = false }
                    )
                }


            }

            // ---------- CART (TABLET ONLY) ----------

            if (!isPhone) {
                Column(
                    modifier = Modifier
                        .width(360.dp) // 🟢 fixed width for right side (adjust as needed)
                        .fillMaxHeight()
                        .background(Color(0xFFF9FAFB)) // optional light background
                        .padding(8.dp)
                ) {
                    RightPanel(
                        cartViewModel = cartViewModel,
                        ordersViewModel = ordersViewModel,
                        tableViewModel = tableVm,
                        orderType = orderType,
                        tableNo = tableId ?: orderType,
                        paymentType = paymentType,
                        onPaymentChange = { paymentType = it },
                        onOrderPlaced = { },
                        onOpenKitchen = { showKitchen = true },
                        onOpenBill = { showBill = true },
                        isMobile = false
                    )
                }
            }


        }

        // ---------- MOBILE CART FAB ----------
        if (isPhone && cartCount > 0) {
            FloatingCartButton(
                count = cartCount,
                onClick = { showCartSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }



    if (isPhone && showCartSheet) {

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true // 🔑 KEY FIX
        )

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showCartSheet = false }
        ) {
            RightPanel(
                cartViewModel = cartViewModel,
                ordersViewModel = ordersViewModel,
                tableViewModel = tableVm,
                orderType = orderType,
                tableNo = tableId ?: orderType,
                paymentType = paymentType,
                onPaymentChange = { paymentType = it },
                onOrderPlaced = { },
                onOpenKitchen = { showKitchen = true },
                onOpenBill = { showBill = true },
                isMobile = true,
                onClose = { showCartSheet = false }
            )
        }
    }

    // ================= KITCHEN POPUP =================
    if (showKitchen) {
        val kitchenKey by cartViewModel.sessionKey.collectAsState()
        val kitchenViewModel: KitchenViewModel = viewModel()

        AlertDialog(
            onDismissRequest = { showKitchen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showKitchen = false }) { Text("Close") }
            },
            title = { Text("Kitchen – $kitchenKey") },
            text = {
                KitchenScreen(
                    tableNo = kitchenKey!!,
                    viewModel = kitchenViewModel,
                    onKitchenEmpty = {
                        // ✅ Close popup automatically when no items
                        showKitchen = false

                    },
                    orderType = orderType
                )
            }
        )
    }


// ================= BILL POPUP =================
    val billingKey by cartViewModel.sessionKey.collectAsState()
    if (showBill && billingKey != null) {
        AlertDialog(
            onDismissRequest = { showBill = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBill = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Billing – $billingKey")
            },
            text = {
                BillScreenDialog(
                    tableId = billingKey!!,   // 🔑 KEY FIX
                    tableViewModel = tableVm,
                    onClose = { showBill = false },
                    orderType = orderType,

                    )
            }
        )
    }



}

// ================= CATEGORY BUTTON =================

@Composable
fun CategoryButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PosGreen else Color.White,
        shape = MaterialTheme.shapes.small,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()   // ✅ prevents full-height bug
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else Color.Black,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}




@Composable
fun FloatingCartButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        FloatingActionButton(
            onClick = onClick,
            containerColor = PosGreen // ✅ ONLY COLOR
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(22.dp)
                    .background(Color.Red, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Composable
fun OrderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PosGreen else Color.White,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun TableSelectorGrid(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Table") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tables) { ui ->

                    val table = ui.table
                    val isSelected = selectedTable == table.id

                    val bgColor = when (ui.color) {
                        PosTableViewModel.TableColor.GREEN -> Color(0xFFDCFCE7)   // 🟢 running
                        PosTableViewModel.TableColor.YELLOW -> Color(0xFFFEF9C3)  // 🟡 bill requested
                        PosTableViewModel.TableColor.RED -> Color(0xFFFEE2E2)     // 🔴 ready to bill
                        PosTableViewModel.TableColor.GRAY -> Color(0xFFF3F4F6)    // ⚪ available
                    }

                    Surface(
                        color = if (isSelected) Color(0xFF16A34A) else bgColor,
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                onTableSelected(table.id)
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // TABLE NAME
                            Text(
                                text = table.tableName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) Color.White else Color.Black
                            )

                            // RUNNING AMOUNT
                            if (ui.runningAmount > 0) {
                                Text(
                                    text = "₹${ui.runningAmount.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun PosOrderTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PosGreen else Color.White,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        border = if (!selected) BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else Color.Black,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }








}
