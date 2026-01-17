package com.it10x.foodappgstav7_02.navigation

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.it10x.foodappgstav7_02.com.ui.settings.PrinterRoleSelectionScreen
import com.it10x.foodappgstav7_02.data.PrinterPreferences
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.pos.AppDatabaseProvider

import com.it10x.foodappgstav7_02.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_02.data.pos.viewmodel.POSOrdersViewModelFactory
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.ui.categories.LocalCategoriesScreen
import com.it10x.foodappgstav7_02.ui.home.HomeScreen
import com.it10x.foodappgstav7_02.ui.orders.online.OnlineOrdersScreen
import com.it10x.foodappgstav7_02.ui.orders.local.LocalOrderDetailScreen
import com.it10x.foodappgstav7_02.ui.orders.local.LocalOrderDetailViewModel
import com.it10x.foodappgstav7_02.ui.orders.local.LocalOrderDetailViewModelFactory
import com.it10x.foodappgstav7_02.ui.orders.local.LocalOrdersScreen


import com.it10x.foodappgstav7_02.ui.pos.PosScreen
import com.it10x.foodappgstav7_02.ui.products.LocalProductsScreen
import com.it10x.foodappgstav7_02.ui.settings.*
import com.it10x.foodappgstav7_02.viewmodel.*
import com.it10x.foodappgstav7_02.data.pos.repository.POSOrdersRepository


import com.it10x.foodappgstav7_02.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import com.it10x.foodappgstav7_02.ui.cart.CartViewModelFactory
import com.it10x.foodappgstav7_02.ui.pos.PosSessionViewModel

@Composable
fun NavigationHost(
    navController: NavHostController,
    printerManager: PrinterManager,
    printerPreferences: PrinterPreferences,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    paddingValues: PaddingValues = PaddingValues(),
    onSavePrinterSettings: () -> Unit = {}
) {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    // -----------------------------
    // SHARED VIEWMODELS
    // -----------------------------

    val printerSettingsViewModel: PrinterSettingsViewModel = viewModel(
        factory = PrinterSettingsViewModelFactory(
            prefs = printerPreferences,
            printerManager = printerManager
        )
    )


    val application = context.applicationContext as Application

    val ordersViewModel: OnlineOrdersViewModel = viewModel(
        factory = OnlineOrdersViewModelFactory(printerManager)
    )
    val posOrdersViewModel: POSOrdersViewModel = viewModel(
        factory = POSOrdersViewModelFactory(
            db = db,
            printerManager = printerManager
        )
    )
    val posSessionViewModel: PosSessionViewModel = viewModel()

    // -----------------------------
    // NAV HOST
    // -----------------------------

    NavHost(
        navController = navController,
        startDestination = "pos",
        modifier = Modifier.padding(paddingValues)
    ) {



        // ---------------- LOCAL PRODUCTS ----------------
        composable("local_products") {
            LocalProductsScreen()
        }

        // ---------------- SYNC ----------------
        composable("sync_data") {
            SyncScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- LOCAL ORDERS ----------------
        composable("local_orders") {

            val localOrdersViewModel: POSOrdersViewModel = viewModel(
                factory = POSOrdersViewModelFactory(
                    db = db,
                    printerManager = printerManager
                )
            )

            LocalOrdersScreen(
                viewModel = localOrdersViewModel,   // ✅ CORRECT TYPE
                navController = navController
            )
        }

        // ---------------- LOCAL ORDER DETAIL ----------------
        composable(
            route = "local_order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments!!.getString("orderId")!!

            val ordersRepository = remember {
                POSOrdersRepository(
                    db = db,                          // ✅ ADD THIS
                    orderMasterDao = db.orderMasterDao(),
                    orderProductDao = db.orderProductDao(),
                    cartDao = db.cartDao(),
                    tableDao = db.tableDao()
                )
            }

            val detailViewModel: LocalOrderDetailViewModel = viewModel(
                factory = LocalOrderDetailViewModelFactory(
                    orderId = orderId,
                    repository = ordersRepository,
                    printerManager = printerManager
                )
            )

            LocalOrderDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }


        // ---------------- CATEGORIES ----------------
        composable("local_categories") {
            LocalCategoriesScreen()
        }

        // ---------------- HOME ----------------
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ---------------- ONLINE ORDERS ----------------
        composable("orders") {
            OnlineOrdersScreen(

                printerManager = printerManager,
                ordersViewModel = ordersViewModel,
                realtimeOrdersViewModel = realtimeOrdersViewModel
            )
        }

        composable("products") { Text("Products Screen") }
        composable("categories") { Text("Categories Screen") }



        // ---------------- POS ----------------

        composable("pos") {

            val context = LocalContext.current
            val db = AppDatabaseProvider.get(context)

            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModelFactory(
                    repository = CartRepository(db.cartDao())
                )
            )

            PosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel
            )
        }



        // ---------------- PRINTER SETTINGS ----------------
        composable("printer_role_selection") {
            PrinterRoleSelectionScreen(
                prefs = printerPreferences,
                onBillingClick = { navController.navigate("printer_settings/BILLING") },
                onKitchenClick = { navController.navigate("printer_settings/KITCHEN") }
            )
        }

        composable(
            "printer_settings/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->

            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )

            PrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                prefs = printerPreferences,
                role = role,
                onSave = {
                    onSavePrinterSettings()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onBluetoothSelected = {
                    navController.navigate("bluetooth_devices/${role.name}")
                },
                onUSBSelected = {
                    navController.navigate("usb_devices/${role.name}")
                },
                onLanSelected = {
                    navController.navigate("lan_printer_settings/${role.name}")
                }
            )
        }

        composable("lan_printer_settings/{role}") { backStackEntry ->
            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )
            LanPrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                role = role,
                onBack = { navController.popBackStack() }
            )
        }

        composable("bluetooth_devices/{role}") {
            val role = PrinterRole.valueOf(
                it.arguments!!.getString("role")!!
            )
            BluetoothDeviceScreen(
                role = role,
                settingsViewModel = printerSettingsViewModel
            )
        }

        composable("usb_devices/{role}") {
            USBPrinterScreen(
                role = PrinterRole.valueOf(
                    it.arguments!!.getString("role")!!
                )
            )
        }

        // ---------------- FINAL BILL ----------------










    }
}
