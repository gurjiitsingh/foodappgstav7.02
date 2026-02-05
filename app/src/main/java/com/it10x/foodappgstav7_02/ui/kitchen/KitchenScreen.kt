package com.it10x.foodappgstav7_02.ui.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
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
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = { viewModel.markDoneAll(orderType,tableNo) }
        ) {
            Text("Send All", color = Color.White)
            Spacer(Modifier.width(4.dp))
            // 🍽️ KOT
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = "KOT",
                tint = Color.White
            )


            Spacer(Modifier.width(4.dp))

            // 🖨️ PRINT
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = "Print",
                tint = Color.White
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
            Spacer(Modifier.width(6.dp))
            // 🧾 BILL
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Bill",
                tint = Color.White
            )
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
                 //  Text("${item.quantity} ${item.name}  ${item.basePrice}")

                    IconButton(
                        onClick = { viewModel.markCancelled(item.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel item",
                            tint = Color(0xFFDC2626) // red
                        )
                    }

                    Text("${item.quantity} ${item.name}")
                    Row {


                        Button(
                            onClick = { viewModel.markDoneNoKotPrint(item.id, orderType) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Move to Bill",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.markDone(item.id, orderType) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            // 🍽️ KOT
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "KOT",
                                tint = Color.White
                            )

                            Spacer(Modifier.width(4.dp))



                            Spacer(Modifier.width(4.dp))

                            // 🖨️ PRINT
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print",
                                tint = Color.White
                            )

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                            // 🧾 BILL
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Bill",
                                tint = Color.White
                            )
                        }

                    }
                }
            }
        }
    }
}