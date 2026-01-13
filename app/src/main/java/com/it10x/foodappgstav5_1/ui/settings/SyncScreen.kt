package com.it10x.foodappgstav5_1.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav5_1.viewmodel.ProductSyncViewModel
import com.it10x.foodappgstav5_1.viewmodel.OutletSyncViewModel
import com.it10x.foodappgstav5_1.viewmodel.TableSyncViewModel

@Composable
fun SyncScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val productVm: ProductSyncViewModel = viewModel()
    val outletVm: OutletSyncViewModel = viewModel()

    val productSyncing by productVm.syncing.collectAsState()
    val productStatus by productVm.status.collectAsState()

    val outletSyncing by outletVm.syncing.collectAsState()
    val outletStatus by outletVm.status.collectAsState()

    val tableVm: TableSyncViewModel = viewModel()
    val tableSyncing by tableVm.syncing.collectAsState()
    val tableStatus by tableVm.status.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ===== HEADER =====
        Text(
            text = "Data Sync & Local Data",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))


        // ===== OUTLET SYNC BUTTON =====
        Button(
            enabled = !outletSyncing && !productSyncing,
            onClick = { outletVm.syncOutlet() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (outletSyncing) "Syncing Outlet…" else "Sync Outlet Config")
        }

        Text(
            text = outletStatus,
            style = MaterialTheme.typography.bodyMedium
        )

        Divider()


        // ===== PRODUCT + CATEGORY SYNC BUTTON =====
        Button(
            enabled = !productSyncing && !outletSyncing,
            onClick = { productVm.syncAll() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (productSyncing) "Syncing Menu…" else "Sync Menu Data")
        }

        Text(
            text = productStatus,
            style = MaterialTheme.typography.bodyMedium
        )


        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Divider()

        Button(
            enabled = !productSyncing && !outletSyncing && !tableSyncing,
            onClick = { tableVm.syncTables() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (tableSyncing) "Syncing Tables…" else "Sync Tables")
        }

        Text(
            text = tableStatus,
            style = MaterialTheme.typography.bodyMedium
        )

        // ===== LOCAL DATA VIEW =====
        Text(
            text = "Local Data",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = { navController.navigate("local_products") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Local Products")
        }

        OutlinedButton(
            onClick = { navController.navigate("local_categories") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Local Categories")
        }

        Spacer(modifier = Modifier.height(24.dp))


        // ===== BACK BUTTON =====
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
