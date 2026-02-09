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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_02.ui.cart.CartUiEvent
import com.it10x.foodappgstav7_02.ui.kitchen.KitchenViewModelFactory

enum class SearchTarget {
    NAME,
    CODE
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    onOpenSettings: () -> Unit,
    ordersViewModel: POSOrdersViewModel,
    posSessionViewModel: PosSessionViewModel,
    posTableViewModel: PosTableViewModel,
) {
    var showTableSelector by rememberSaveable() {
        mutableStateOf(false)
    }


    var showSearchKeyboard by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600

    var orderType by remember { mutableStateOf("DINE_IN") }

    val sessionId by cartViewModel.sessionKey.collectAsState()
    val tableId1 by posSessionViewModel.tableId.collectAsState()
    val tableId =  tableId1 ?:""
    // val tables by tableVm.tables.collectAsState()
    val tables by posTableViewModel.tables.collectAsState()

    val tableVm: PosTableViewModel = viewModel()

    val selectedTableName1 = tables
        .firstOrNull { it.table.id == tableId }
        ?.table
        ?.tableName
 var selectedTableName = selectedTableName1 ?: ""

    var activeTarget by rememberSaveable { mutableStateOf(SearchTarget.NAME) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var codeQuery by rememberSaveable { mutableStateOf("") }

    val repository = POSOrdersRepository(
        db = db,
        orderMasterDao = db.orderMasterDao(),
        orderProductDao = db.orderProductDao(),
        cartDao = db.cartDao(),
        tableDao = db.tableDao()
    )

    LaunchedEffect(Unit) {
        cartViewModel.uiEvent.collect { event ->
            when (event) {

                CartUiEvent.SessionRequired -> {
                    if (orderType == "DINE_IN") {
                        showTableSelector = true
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
    LaunchedEffect(categories) {
        if (selectedCatId == null && categories.isNotEmpty()) {
            selectedCatId = categories.first().id
        }
    }



    LaunchedEffect(Unit) { tableVm.loadTables() }


    val filteredProducts = remember(
        parentProducts,
        selectedCatId,
        searchQuery,
        codeQuery,
        activeTarget
    ) {
        val query = when (activeTarget) {
            SearchTarget.NAME -> searchQuery.trim().lowercase()
            SearchTarget.CODE -> codeQuery.trim().lowercase()
        }

        if (query.isNotEmpty()) {
            parentProducts.filter { product ->
                when (activeTarget) {
                    SearchTarget.NAME -> product.name.lowercase().contains(query)  // partial match for name
                    SearchTarget.CODE -> product.searchCode?.lowercase() == query   // exact match for code
                }
            }
        } else {
            if (selectedCatId == null) emptyList()
            else parentProducts.filter { it.categoryId == selectedCatId }
        }
    }



//    val filteredProducts = remember(
//        parentProducts,
//        selectedCatId,
//        searchQuery,
//        codeQuery,
//        activeTarget
//    ) {
//        val query = when (activeTarget) {
//            SearchTarget.NAME -> searchQuery.trim().lowercase()
//            SearchTarget.CODE -> codeQuery.trim().lowercase()
//        }
//
//        if (query.isNotEmpty()) {
//            parentProducts.filter { product ->
//                when (activeTarget) {
//                    SearchTarget.NAME -> product.name.lowercase().contains(query)
//                    SearchTarget.CODE -> product.searchCode?.lowercase()?.contains(query) == true
//                }
//            }
//        } else {
//            if (selectedCatId == null) emptyList()
//            else parentProducts.filter { it.categoryId == selectedCatId }
//        }
//    }



//    val filteredProducts = remember(
//        parentProducts,
//        selectedCatId,
//        searchQuery
//    ) {
//        val query = searchQuery.trim().lowercase()
//
//        if (query.isNotEmpty()) {
//            parentProducts.filter { product ->
//                product.name.lowercase().contains(query) ||
//                        product.searchCode?.lowercase()?.contains(query) == true
//            }
//        } else {
//            if (selectedCatId == null) {
//                emptyList()
//            } else {
//                parentProducts.filter { it.categoryId == selectedCatId }
//            }
//        }
//    }





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
            .background(MaterialTheme.colorScheme.background)
    ) {

        Row(modifier = Modifier.fillMaxSize()) {

            // ---------- LEFT CATEGORY SIDEBAR ----------
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(15.dp)   // ✅ SAME AS PRODUCTS
            ) {



                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(categories) { c ->
                        CategoryButton(
                            label = toTitleCase(c.name),
                            selected = selectedCatId == c.id,
                          //  showSearchKeyboard = false
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



                // ---------- SEARCH BOX ----------



// ---------- SEARCH ROW(S) ----------
                        if (isPhone) {
                            // PHONE: TWO ROWS
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // ROW 1: NAME SEARCH
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeTarget = SearchTarget.NAME
                                            codeQuery = ""
                                            showSearchKeyboard = true
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            searchQuery = it
                                            codeQuery = "" // clear other box
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(53.dp),
                                        placeholder = { Text("Search by name") },
                                        singleLine = true
                                    )

                                }

                                // ROW 2: CODE SEARCH + CLEAR
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                activeTarget = SearchTarget.CODE
                                                searchQuery = ""
                                                showSearchKeyboard = true
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = codeQuery,
                                            onValueChange = {
                                                codeQuery = it
                                                searchQuery = ""
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(53.dp),
                                            placeholder = { Text("Search by code") },
                                            singleLine = true
                                        )

                                    }

                                    // SMALL LIGHT GRAY CLEAR BUTTON WITH X ICON
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            codeQuery = ""
                                        },
                                        modifier = Modifier
                                            .size(36.dp) // smaller
                                            .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        } else {
                            // TABLET/DESKTOP: SINGLE ROW
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // NAME SEARCH
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeTarget = SearchTarget.NAME
                                            codeQuery = ""
                                            showSearchKeyboard = true
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(53.dp),
                                        placeholder = { Text("Search by name") },
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false
                                    )
                                }

                                // CODE SEARCH

                                Box(
                                    modifier = Modifier
                                        .weight(0.9f) // 👈 was 1f — slightly shorter
                                        .clickable {
                                            activeTarget = SearchTarget.CODE
                                            searchQuery = ""
                                            showSearchKeyboard = true
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = codeQuery,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(53.dp),

                                        placeholder = { Text("Search by code") },
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false
                                    )
                                }

                                // CLEAR BUTTON
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        codeQuery = ""
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color.LightGray.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White // 👈 white X
                                    )
                                }
                            }
                        }






                ProductList(
                    filteredProducts = filteredProducts,
                    variants = variants,
                    cartViewModel = cartViewModel,
                    tableViewModel = tableVm,
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
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                ) {

                    // ---------- CART (ALWAYS VISIBLE) ----------
                    RightPanel(
                        cartViewModel = cartViewModel,
                        ordersViewModel = ordersViewModel,
                        tableViewModel = tableVm,
                        orderType = orderType,
                        tableNo = tableId ?: orderType,
                        tableName = selectedTableName,
                        paymentType = paymentType,
                        onPaymentChange = { paymentType = it },
                        onOrderPlaced = {
                            showSearchKeyboard = false
                        },
                        onOpenKitchen = { showKitchen = true },
                        onOpenBill = { showBill = true },
                        isMobile = false,
                        repository = repository
                    )

                    // ---------- FLOATING KEYBOARD ----------
                    if (showSearchKeyboard) {

                        // 1️⃣ Click-outside dismiss layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showSearchKeyboard = false
                                }
                        )

                        // 2️⃣ Keyboard panel
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(LocalConfiguration.current.screenHeightDp.dp * 0.65f) // 🔥 taller
                                .background(MaterialTheme.colorScheme.surface)
                                .zIndex(11f)
                        ) {
//                            PosSearchKeyboardRight(
//                                onKeyPress = { searchQuery += it },
//                                onBackspace = {
//                                    if (searchQuery.isNotEmpty())
//                                        searchQuery = searchQuery.dropLast(1)
//                                },
//                                onClear = { searchQuery = "" },
//                                onClose = { showSearchKeyboard = false }
//                            )

                            PosSearchKeyboardRight(

                                onKeyPress = { char ->
                                    when (activeTarget) {
                                        SearchTarget.NAME -> searchQuery += char
                                        SearchTarget.CODE -> codeQuery += char
                                    }
                                },

                                onBackspace = {
                                    when (activeTarget) {
                                        SearchTarget.NAME ->
                                            if (searchQuery.isNotEmpty())
                                                searchQuery = searchQuery.dropLast(1)

                                        SearchTarget.CODE ->
                                            if (codeQuery.isNotEmpty())
                                                codeQuery = codeQuery.dropLast(1)
                                    }
                                },

                                onClear = {
                                    when (activeTarget) {
                                        SearchTarget.NAME -> searchQuery = ""
                                        SearchTarget.CODE -> codeQuery = ""
                                    }
                                },

                                onClose = { showSearchKeyboard = false }
                            )

                        }

                    }
                }
            }



        }

        // ---------- MOBILE CART FAB ----------
//        if (isPhone && cartCount > 0) {
        if (isPhone) {
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
                tableName = selectedTableName,
                paymentType = paymentType,
                onPaymentChange = { paymentType = it },
                onOrderPlaced = { },
                onOpenKitchen = { showKitchen = true },
                onOpenBill = { showBill = true },
                isMobile = true,
                onClose = { showCartSheet = false },
                repository = repository
            )
        }
    }

    // ================= KITCHEN POPUP =================
    if (showKitchen && sessionId != null) {
      //  val kitchenKey by cartViewModel.sessionKey.collectAsState()
        val kitchenTitle = when (orderType) {
            "DINE_IN" -> "Table ${tableId ?: ""}"
            "TAKEAWAY" -> "Takeaway"
            "DELIVERY" -> "Delivery"
            else -> sessionId
        }

        val kitchenViewModel: KitchenViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            key = "KitchenVM_${sessionId ?: orderType}",
            factory = KitchenViewModelFactory(
                app = LocalContext.current.applicationContext as android.app.Application,
                tableId = tableId ?: orderType,
                tableName = selectedTableName!!,
                sessionId = sessionId!!,
                orderType = orderType,
                repository = repository,
                cartViewModel = cartViewModel
            )
        )

        AlertDialog(
            onDismissRequest = { showKitchen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showKitchen = false }) { Text("Close") }
            },
            title = {
                Text("Kitchen – $tableName")
            },
            text = {
                KitchenScreen(
                    sessionId = sessionId!!,   // for DB / logic
                    tableNo = tableId ?: orderType, // for UI / print
                    tableName = selectedTableName!!,
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
  //  val billingKey by cartViewModel.sessionKey.collectAsState()
    if (showBill && sessionId != null) {


        AlertDialog(
            onDismissRequest = { showBill = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBill = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Billing")
            },
            text = {
                BillScreenDialog(
                    sessionId = sessionId!!,   // for DB / logic
                    tableId = tableId ?: orderType, // for UI / print // 🔑 KEY FIX
                    tableName = selectedTableName!!,
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
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        shadowElevation = 2.dp,
        border = if (!selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else null,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
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
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = MaterialTheme.colorScheme.onPrimary
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
                    color = MaterialTheme.colorScheme.onPrimary,
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
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}




@Composable
fun PosOrderTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
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
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }








}
fun toTitleCase(text: String): String {
    return text
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}
