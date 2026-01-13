package com.it10x.foodappgstav5_1.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.ui.cart.CartViewModel
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {
                        Text(item.name)
                        Text(
                            "₹${item.basePrice} x ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(
                            onClick = {
                                cartViewModel.decrease(item.productId)
                            }
                        ) {
                            Text("−")
                        }

                        Text(
                            text = item.quantity.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                cartViewModel.increase(item)
                            }
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        }
    }
}
