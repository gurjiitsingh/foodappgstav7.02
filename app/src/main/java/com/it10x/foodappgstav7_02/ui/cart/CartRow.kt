package com.it10x.foodappgstav7_02.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.ui.Alignment
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.ui.theme.PosError

@Composable
fun CartRow(
    item: PosCartEntity,
    tableNo: String,
    cartViewModel: CartViewModel,
    onCartActionDirectMoveToBill: (item: PosCartEntity, print: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {

            // ================= ROW 1 =================
            // Item name | Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "₹${item.basePrice}",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= ROW 2 =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Bill + KOT
                Button(
                    modifier = Modifier.weight(1.7f),
                    onClick = { onCartActionDirectMoveToBill(item, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = PosError)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Bill and kitchen print",
                        tint = Color.White
                    )

                    Spacer(Modifier.width(6.dp))


                    Icon(
                        imageVector = Icons.Default.SoupKitchen, // ⬅️ change to LocalDining if you prefer
                        contentDescription = "Send to Kitchen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print",
                        modifier = Modifier.size(16.dp)
                    )

//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "Send",
//                        modifier = Modifier.size(14.dp),
//                        tint = Color.White
//                    )
                }


                // Bill only
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onCartActionDirectMoveToBill(item, false) },
                    border = BorderStroke(1.5.dp, PosError),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "only Bill",
                        tint = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(4.dp))
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            cartViewModel.decrease(item.productId, tableNo)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDC2626))
                    ) {
                        Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { cartViewModel.addToCart(item) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF16A34A))
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

