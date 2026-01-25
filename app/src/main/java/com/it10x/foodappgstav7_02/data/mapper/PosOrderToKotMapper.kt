package com.it10x.foodappgstav7_02.data.mapper

import android.util.Log
import com.it10x.foodappgstav7_02.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_02.data.pos.entities.PosOrderItemEntity
import java.util.*

/**
 * Mapper to convert POS Order Items → KOT Items
 * (Used when reprinting or resending items to kitchen)
 */
object PosOrderToKotMapper {

    fun toKotItems(
        orderItems: List<PosOrderItemEntity>,
        kotBatchId: String = "POS",
        tableNo: String? = null
    ): List<PosKotItemEntity> {
        val now = System.currentTimeMillis()

        Log.d("PRINT", "Mapping ${orderItems.size} order items to KOT items...")

        orderItems.forEachIndexed { index, item ->
            Log.d(
                "PRINT",
                "Item[$index]: name='${item.name}', qty=${item.quantity}, base=${item.basePrice}, tax=${item.taxRate}"
            )
        }

        return orderItems.map { item ->
            PosKotItemEntity(
                id = UUID.randomUUID().toString(),
                kotBatchId = kotBatchId,
                tableNo = tableNo ?: "POS",

                productId = item.productId,
                name = item.name,
                categoryId = item.categoryId,

                parentId = item.parentId,
                isVariant = item.isVariant,

                basePrice = item.basePrice,
                quantity = item.quantity,

                taxRate = item.taxRate,
                taxType = item.taxType,

                status = "DONE",
                isPrinted = false,
                createdAt = now
            )
        }
    }
}
