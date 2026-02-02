package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment

@Composable
fun CartScreen(
    cartViewModel: CartViewModel
) {
    val cartItems by cartViewModel.cart.collectAsState(
        initial = emptyList()
    )

    Column {

        Text("Cart", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (cartItems.isEmpty()) {
            Text("Cart is empty")
            return@Column
        }

        LazyColumn {
            items(cartItems) { item ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    // ---------- MAIN ROW: PRODUCT INFO + QUANTITY ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // PRODUCT INFO
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(item.name)
                            Text(
                                "₹${item.basePrice} x ${item.quantity}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // QUANTITY CONTROLS
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { cartViewModel.decrease(item.productId) }) {
                                Text("−")
                            }

                            Text(
                                text = item.quantity.toString(),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(onClick = { cartViewModel.increase(item) }) {
                                Text("+")
                            }
                        }
                    }

                    // ---------- DIRECT TO BILL BUTTONS ROW ----------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Add to bill + KOT Print */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("→ Bill + KOT Print")
                        }

                        Button(
                            onClick = { /* TODO: Add to bill only */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("→ Bill")
                        }
                    }

                    Divider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }




    }
}
