package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_02.data.local.entities.ProductEntity
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalLayoutApi::class)
@Composable

fun ProductList(
    filteredProducts: List<ProductEntity>,
    variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableNo: String  // add this
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {

        items(
            count = filteredProducts.size,
            span = { index ->
                val product = filteredProducts[index]
                if (product.hasVariants == true) {
                    GridItemSpan(maxLineSpan) // ✅ FULL WIDTH
                } else {
                    GridItemSpan(1)           // normal grid cell
                }
            }
        ) { index ->

            val product = filteredProducts[index]

            Card(
                modifier = Modifier.fillMaxWidth(),
               // border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                ProductInnerContent(
                    product = product,
                    variants = variants,
                    cartViewModel = cartViewModel,
                    tableNo = tableNo ?: "T0"  // fallback if null
                )
            }
        }
    }


}





@Composable
private fun ProductInnerContent(
    product: ProductEntity,
    variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableNo : String
) {
    val variants = remember(product.id, variants) {
        variants.filter {
            it.parentId == product.id && it.type == "variant"
        }
    }

    Column(Modifier.padding(5.dp)) {

        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {

            if (variants.isNotEmpty()) {
                items(variants, key = { it.id }) { v ->
                    VariantCard(v, cartViewModel, tableNo)
                }
            }

            if (product.parentId == null && product.hasVariants == false ) {
                item {
                    ParentProductCard(product, cartViewModel,tableNo)
                }
            }
        }
    }
}


@Composable
private fun ParentProductCard(
    product: ProductEntity,
    cartViewModel: CartViewModel,
    tableNo: String
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                1.dp,
                Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ⭐ PRODUCT NAME
            Text(
                text = product.name,
                minLines = 2,      // ⭐ always reserve 2 lines height
                maxLines = 2,      // ⭐ never exceed 2 lines
                lineHeight = 18.sp // ⭐ optional but recommended for consistency
            )

            // ⭐ PRODUCT PRICE
            Text(
                "₹${product.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖ decrease
                IconButton(
                    onClick = { cartViewModel.decrease(product.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("−", color = Color.White, fontSize = 18.sp)
                }

                // ➕ add to cart
                IconButton(
                    onClick = {
                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = product.name,
                                basePrice = product.price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",
                                parentId = null,
                                isVariant = false,
                                categoryId = product.categoryId,
                                tableId = tableNo
                            )
                        )
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("+", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    product: ProductEntity,
    cartViewModel: CartViewModel,
    tableNo: String
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                1.dp,
                Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = product.name,
                minLines = 2,      // ⭐ always reserve 2 lines height
                maxLines = 2,      // ⭐ never exceed 2 lines
                lineHeight = 18.sp // ⭐ optional but recommended for consistency
            )

            Text(
                "₹${product.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ➖
                IconButton(
                    onClick = { cartViewModel.decrease(product.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("−", color = Color.White, fontSize = 18.sp)
                }

                // ➕
                IconButton(
                    onClick = {
                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = product.name,
                                basePrice = product.price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",

                                parentId = product.parentId,
                                isVariant = product.parentId != null,

                                categoryId = product.categoryId,
                                tableId = tableNo  // ✅ pass selected table
                            )
                        )
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                ) {
                    Text("+", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}
