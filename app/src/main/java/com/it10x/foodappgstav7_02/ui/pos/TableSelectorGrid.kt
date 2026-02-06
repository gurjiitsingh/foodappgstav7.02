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
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel


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
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        tonalElevation = 2.dp,
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {

                                if (ui.cartCount > 0) {
                                    Text(
                                        text = "🛒 ${ui.cartCount}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                if (ui.kitchenPendingCount > 0) {
                                    Text(
                                        text = "🍳 ${ui.kitchenPendingCount}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                if (ui.billAmount > 0) {
                                    Text(
                                        text = "₹${ui.billAmount.toInt()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (ui.isBilled)
                                            Color(0xFF2E7D32) // green
                                        else
                                            Color(0xFFD32F2F) // red
                                    )
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
