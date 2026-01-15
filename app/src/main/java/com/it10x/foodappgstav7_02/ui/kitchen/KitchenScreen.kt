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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KitchenScreen(
    tableNo: String,
    viewModel: KitchenViewModel,
    onKitchenEmpty: () -> Unit
) {
    val items by viewModel
        .getPendingItems(tableNo)
        .collectAsState(initial = emptyList())

    if (items.isEmpty()) {
        onKitchenEmpty()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // 🔥 FIXED TOP BUTTON (NOT SCROLLING)
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            onClick = { viewModel.markDoneAll(tableNo) }
        ) {
            Text("Done All", color = Color.White)
        }

        // 🔹 LIST TAKES REMAINING SPACE ONLY
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)   // ⭐ THIS IS THE KEY FIX
        ) {
            items(items, key = { it.id }) { item ->

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
                            onClick = { viewModel.markDone(item.id) },
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
