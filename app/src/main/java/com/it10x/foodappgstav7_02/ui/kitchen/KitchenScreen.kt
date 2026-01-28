package com.it10x.foodappgstav7_02.ui.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KitchenScreen(
    sessionId: String,
    tableNo: String,
    orderType: String, // ✅ ADD THIS
    viewModel: KitchenViewModel,
    onKitchenEmpty: () -> Unit
) {
    //MODIFY TO FIND BY SESSION ID
    val items by viewModel.getPendingItems(tableNo, orderType).collectAsState(initial = null)

    // ✅ Only run close check AFTER we actually received a list from DB
    LaunchedEffect (items) {
        if (items != null && items!!.isEmpty()) {
            onKitchenEmpty()
        }
    }

    // ✅ Handle loading phase gracefully
    if (items == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(10.dp))
            Text("Loading kitchen items…")
        }
        return
    }

    // ✅ Handle empty AFTER load
    if (items!!.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("No pending items for this order.")
        }
        return
    }

    // ✅ Normal UI when items exist
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = { viewModel.markDoneAll(orderType,tableNo) }
        ) {
            Text("Done All", color = Color.White)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(items!!, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.name} x${item.quantity}")

                    Row {
                        Button(
                            onClick = { viewModel.markCancelled(item.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626)
                            )
                        ) {
                            Text("Cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.markDone(item.id, orderType) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}