package com.it10x.foodappgstav5_1.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav5_1.data.local.AppDatabaseProvider
import com.it10x.foodappgstav5_1.data.local.viewmodel.*

@Composable
fun LocalCategoriesScreen() {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val vm: CategoriesLocalViewModel = viewModel(
        factory = CategoriesLocalViewModelFactory(db.categoryDao())
    )

    val categories by vm.categories.collectAsState()

    Column(Modifier.padding(16.dp)) {

        Text("Local Categories", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(10.dp))

        if (categories.isEmpty()) {
            Text("No categories synced yet")
        } else {
            categories.forEach {
                Text("• ${it.name}")
            }
        }
    }
}
