package com.it10x.foodappgstav7_02.ui.pos


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel

import androidx.compose.ui.text.TextStyle

@Composable
fun TableSelectorGrid(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Table") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tables) { ui ->

                    val table = ui.table
                    val isSelected = selectedTable == table.id

                    val bgColor = when (ui.color) {
                        PosTableViewModel.TableColor.GREEN ->
                            Color(0xFF4CAF50).copy(alpha = 0.20f)

                        PosTableViewModel.TableColor.BLUE ->
                            Color(0xFF2196F3).copy(alpha = 0.20f)

                        PosTableViewModel.TableColor.RED ->
                            Color(0xFFF44336).copy(alpha = 0.20f)

                        PosTableViewModel.TableColor.GRAY ->
                            Color(0xFFBDBDBD).copy(alpha = 0.20f)
                    }

                    Surface(
                        color = bgColor,
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        border = if (isSelected)
                            BorderStroke(3.dp, Color(0xFFFF9800)) // 🟠 selection
                        else null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onTableSelected(table.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // 🔹 TABLE NAME
                            Text(
                                text = table.tableName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // 🔹 STATUS INFO
                            // 🔹 STATUS INFO (CART + KITCHEN + BILL)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // 🛒 CART
                                    if (ui.cartCount > 0) {
                                        StatusBadge(
                                            icon = "🛒",
                                            text = ui.cartCount.toString(),
                                            bgColor = Color(0xFF1976D2),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // 🍳 KITCHEN
                                    if (ui.kitchenPendingCount > 0) {
                                        StatusBadge(
                                            icon = "🍳",
                                            text = ui.kitchenPendingCount.toString(),
                                            bgColor = Color(0xFFF9A825),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // 💰 BILL COUNT
                                    if (ui.billDoneCount > 0) {
                                        StatusBadge(
                                            icon = "🧾", // bill icon
                                            text = ui.billDoneCount.toString(),
                                            bgColor = Color(0xFF2E7D32),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // 💵 BILL AMOUNT
                                    if (ui.billAmount > 0) {
                                        StatusBadge(
                                            icon = "",
                                            text = ui.billAmount.toInt().toString(),
                                            bgColor = if (ui.isBilled)
                                                Color(0xFF2E7D32)
                                            else
                                                Color(0xFFD32F2F),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
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
fun StatusBadge(
    icon: String,
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 1.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon.isNotEmpty()) {
                Text(icon)
                Spacer(Modifier.width(1.dp))
            }

            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = textStyle
            )
        }
    }
}


