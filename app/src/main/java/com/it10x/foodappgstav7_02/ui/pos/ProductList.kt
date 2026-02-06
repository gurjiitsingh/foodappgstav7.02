package com.it10x.foodappgstav7_02.ui.pos

import android.util.Log
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
import com.it10x.foodappgstav7_02.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.ui.cart.CartViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.it10x.foodappgstav7_02.viewmodel.PosTableViewModel


@OptIn(ExperimentalLayoutApi::class)
@Composable

fun ProductList(
    filteredProducts: List<ProductEntity>,
    variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String?,  // add this
    posSessionViewModel: PosSessionViewModel  // 🔑 add this
    ) {
    val sessionId by posSessionViewModel.sessionId.collectAsState()


    // ✅ Sort the products before displaying
    val sortedProducts = remember(filteredProducts) {
        filteredProducts.sortedBy { it.sortOrder }
    }

//    LazyVerticalGrid(
//        columns = GridCells.Adaptive(minSize = 160.dp),
//        modifier = Modifier.fillMaxSize(),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = PaddingValues(4.dp)
//    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterHorizontally // ⭐ FIX
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp) // optional cleaner edge
    )
    {

        items(
            count = sortedProducts.size,
            span = { index ->
                val product = sortedProducts[index]
                if (product.hasVariants == true) {
                    GridItemSpan(maxLineSpan) // ✅ FULL WIDTH
                } else {
                    GridItemSpan(1)           // normal grid cell
                }
            }
        ) { index ->

            val product = sortedProducts[index]

            Card(
                modifier = Modifier.fillMaxWidth(),
       //         border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surface
//                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                ProductInnerContent(
                    product = product,
                    variants = variants,
                    cartViewModel = cartViewModel,
                    tableViewModel = tableViewModel,
                    tableNo = tableNo ?: "T0",  // fallback if null
                    sessionId = sessionId  // 🔑 pass here
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
    tableViewModel: PosTableViewModel,
    tableNo : String,
    sessionId: String   // 🔑 add this
) {
    val variants = remember(product.id, variants) {
        variants.filter {
            it.parentId == product.id && it.type == "variant"
        }
    }

    Column(Modifier.padding(1.dp)) {

        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {

            if (variants.isNotEmpty()) {
                items(variants, key = { it.id }) { v ->
                    VariantCard(v, cartViewModel, tableViewModel, tableNo,sessionId)
                }
            }

            if (product.parentId == null && product.hasVariants == false ) {
                item {
                    ParentProductCard(product, cartViewModel, tableViewModel,tableNo,  sessionId)
                }
            }
        }
    }
}


@Composable
private fun ParentProductCard(
    product: ProductEntity,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    sessionId: String   // 🔑 add this
) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline),
//            .border(
//                1.dp,
//                Color(0xFFE0E0E0),
//                shape = MaterialTheme.shapes.small
//            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            var price: Double = 0.0

            price = when {
                product.discountPrice == null || product.discountPrice == 0.0 -> product.price
                else -> product.discountPrice
            }

            // ⭐ PRODUCT NAME
            Text(
                text = toTitleCase(product.name),
                minLines = 2,      // ⭐ always reserve 2 lines height
                maxLines = 2,      // ⭐ never exceed 2 lines
                lineHeight = 18.sp // ⭐ optional but recommended for consistency
            )

            // ⭐ PRODUCT PRICE
            Text(
                "₹${price}",
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
                    onClick = { cartViewModel.decrease(product.id, tableNo) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                        .border(1.5.dp, Color(0xFFD32F2F), MaterialTheme.shapes.small) // ✅ red border
                ) {
                    Text(
                        text = "−",
                        color = Color(0xFFD32F2F), // ✅ bold red text
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ➕ add to cart
                IconButton(
                    onClick = {

                        Log.d(
                            "CART_DEBUG",
                            "In Productlist:markOrdering:   "
                        )

                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = toTitleCase(product.name),
                                basePrice = price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",
                                parentId = null,
                                isVariant = false,
                                categoryId = product.categoryId,
                                sessionId = sessionId ?: "POS_DEFAULT",  // 🔑 pass sessionId
                                tableId = tableNo  // still optional
                            )
                        )

                        tableNo?.let {
                            tableViewModel.markOrdering(it)
                        }
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
    tableViewModel: PosTableViewModel,
    tableNo: String,
    sessionId: String,   // 🔑 add this

) {

    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            var price: Double = 0.0

            price = when {
                product.discountPrice == null || product.discountPrice == 0.0 -> product.price
                else -> product.discountPrice
            }

            Text(
                text = toTitleCase(product.name),
                minLines = 2,      // ⭐ always reserve 2 lines height
                maxLines = 2,      // ⭐ never exceed 2 lines
                lineHeight = 18.sp // ⭐ optional but recommended for consistency
            )

            Text(
                "₹${price}",
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
                    onClick = { cartViewModel.decrease(product.id, tableNo) },
                    modifier = Modifier
                        .size(32.dp)
                        //.background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Text("-", color = MaterialTheme.colorScheme.onError)
                }

                // ➕
                IconButton(
                    onClick = {


                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = toTitleCase(product.name),
                                basePrice = price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",

                                parentId = product.parentId,
                                isVariant = product.parentId != null,

                                categoryId = product.categoryId,
                                sessionId = sessionId ?: "POS_DEFAULT",  // 🔑 pass sessionId
                                tableId = tableNo  // ✅ pass selected table
                            )
                        )



                        tableNo?.let {

                            tableViewModel.markOrdering(it)
                        }


                    },
                    modifier = Modifier
                        .size(32.dp)
                       // .background(Color(0xFFD32F2F), MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                   // Text("+", color = Color.White, fontSize = 18.sp)
                    Text("+", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

