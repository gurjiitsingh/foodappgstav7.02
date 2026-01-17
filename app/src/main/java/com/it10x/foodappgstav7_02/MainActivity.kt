package com.it10x.foodappgstav7_02

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import com.it10x.foodappgstav7_02.data.PrinterPreferences
import com.it10x.foodappgstav7_02.data.online.models.repository.OrdersRepository
import com.it10x.foodappgstav7_02.printer.PrinterManager
import com.it10x.foodappgstav7_02.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_02.viewmodel.RealtimeOrdersViewModel
import com.it10x.foodappgstav7_02.navigation.NavigationHost
import com.it10x.foodappgstav7_02.printer.AutoPrintManager
import com.it10x.foodappgstav7_02.service.OrderListenerService

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import com.it10x.foodappgstav7_02.firebase.ClientIdStore
import com.it10x.foodappgstav7_02.ui.settings.ClientSetupScreen


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val serviceIntent = Intent(this, OrderListenerService::class.java)
//        startForegroundService(serviceIntent)





        setContent {

            val context = LocalContext.current
            val clientId = remember { ClientIdStore.get(context) }

            if (clientId == null) {
                MaterialTheme {
                    ClientSetupScreen()
                }
                return@setContent
            }


            // ✅ START SERVICE ONLY NOW
            LaunchedEffect(Unit) {
                val serviceIntent = Intent(context, OrderListenerService::class.java)
                context.startForegroundService(serviceIntent)
            }
// ------------------------------------
// CORE SINGLETON OBJECTS (ONCE)
// ------------------------------------
            val printerPreferences = remember { PrinterPreferences(this) }
            val printerManager = remember { PrinterManager(this) }
            val ordersRepository = remember { OrdersRepository() }

            val ordersViewModel: OnlineOrdersViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return OnlineOrdersViewModel(printerManager) as T
                    }
                }
            )

            val autoPrintManager = remember {
                AutoPrintManager(
                    printerManager = printerManager,
                    ordersRepository = ordersRepository
                )
            }

            // ------------------------------------
            // REALTIME ORDERS VIEWMODEL (FACTORY)
            // ------------------------------------
            val realtimeOrdersVM: RealtimeOrdersViewModel =
                viewModel(factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RealtimeOrdersViewModel(
                            application = application,
                            autoPrintManager = autoPrintManager
                        ) as T
                    }
                })



            // ------------------------------------
            // UI STATE
            // ------------------------------------
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {

                        // ===== HEADER =====
                        Text(
                            "Menu",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )

                        // ===============================
                        // OPERATIONS
                        // ===============================
                        SidebarSectionHeader("OPERATIONS")


                        NavigationDrawerItem(
                            label = { Text("POS") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("pos") {
                                    popUpTo("pos") { inclusive = true }
                                }
                            }
                        )


                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 4.dp),
                            thickness = 0.5.dp
                        )
                        NavigationDrawerItem(
                            label = { Text("Online Orders") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("orders")
                            }
                        )
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 4.dp),
                            thickness = 0.5.dp
                        )
                        NavigationDrawerItem(
                            label = { Text("Local Orders") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("local_orders")
                            }
                        )



                        // ===============================
                        // SYNC & DATA
                        // ===============================


                        SidebarSectionHeader("SYNC & DATA")

                        NavigationDrawerItem(
                            label = { Text("Sync") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("sync_data")
                            }
                        )



                        // ===============================
                        // SETTINGS
                        // ===============================
                        SidebarSectionHeader("SETTINGS")

                        NavigationDrawerItem(
                            label = { Text("Printer Settings") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("printer_role_selection")
                            }
                        )

                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 4.dp),
                            thickness = 0.5.dp
                        )
                    }




                }
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                           // title = { Text("POS") },
                            title = {  },
                            // LEFT SIDE → SIDEBAR
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu"
                                    )
                                }
                            },

                            // RIGHT SIDE → 3 TEXT BUTTONS
                            actions = {

                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("pos") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                ) {
                                    Text("POS")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("local_orders")
                                    }
                                ) {
                                    Text("ORDERS")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StopSoundButton(viewModel = realtimeOrdersVM)
                            }
                        )
                    }
                ) { paddingValues ->

                    NavigationHost(
                        navController = navController,
                        printerManager = printerManager,
                        printerPreferences = printerPreferences,
                        realtimeOrdersViewModel = realtimeOrdersVM,
                        paddingValues = paddingValues,
                        onSavePrinterSettings = { }
                    )
                }

            }
        }
    }

    @Composable
    fun StopSoundButton(viewModel: RealtimeOrdersViewModel) {

        val context = LocalContext.current

        Button(
            onClick = {

                // 1️⃣ stop ringtone in ACTIVITY
                viewModel.stopRingtone()

                // 2️⃣ stop ringtone in SERVICE
                val intent = Intent("STOP_RINGTONE")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("STOP SOUND")
        }
    }


    @Composable
    fun StopAlertButtonNew(viewModel: RealtimeOrdersViewModel) {

        val context = LocalContext.current

        Button(
            onClick = {
                viewModel.stopRingtone()

                val intent = Intent("STOP_RINGTONE")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("STOP ALERT")
        }
    }


}


@Composable
fun SidebarSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16A34A)) // Tailwind green-600
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
    Spacer(modifier = Modifier.height(4.dp)) // small separation
}



