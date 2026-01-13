package com.it10x.foodappgstav5_1.ui.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
    viewModel: KitchenViewModel
) {
    val items by viewModel
        .getPendingItems(tableNo)
        .collectAsState(initial = emptyList())

    LazyColumn {
        items(items, key = { it.id }) { item ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("${item.name} x${item.quantity}")

                Row {

                    // ❌ CANCEL
                    Button (
                        onClick = { viewModel.markCancelled(item.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626)
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    // ✅ DONE
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

