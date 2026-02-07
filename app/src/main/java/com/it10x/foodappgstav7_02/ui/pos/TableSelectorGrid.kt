package com.it10x.foodappgstav7_02.ui.pos


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
   // val groupedByArea = tables.groupBy { it.table.area ?: "General" }

    // ✅ Group tables by area and sort by sortOrder
    val groupedByArea = tables
        .groupBy { it.table.area ?: "General" }
        .mapValues { (_, areaTables) ->
            areaTables.sortedBy { it.table.sortOrder ?: Int.MAX_VALUE }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Table") },
        text = {
            // ✅ use ScrollColumn for stable height
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 700.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(6.dp)
            ) {
                groupedByArea.entries.forEach { (areaName, areaTables) ->

                    // 🔹 Area Title
                    Text(
                        text = areaName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                    )

                    // 🔹 Grid for each area — userScroll disabled (static height)
                    val rows = (areaTables.size + 4) / 5
                    val gridHeight = (rows * 110).dp

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(areaTables) { ui ->
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
                                    BorderStroke(3.dp, Color(0xFFFF9800))
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
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (ui.cartCount > 0) {
                                                StatusBadge(
                                                    icon = "🛒",
                                                    text = ui.cartCount.toString(),
                                                    bgColor = Color(0xFF1976D2),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

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
                                            if (ui.billDoneCount > 0) {
                                                StatusBadge(
                                                    icon = "🧾",
                                                    text = ui.billDoneCount.toString(),
                                                    bgColor = Color(0xFF2E7D32),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

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


