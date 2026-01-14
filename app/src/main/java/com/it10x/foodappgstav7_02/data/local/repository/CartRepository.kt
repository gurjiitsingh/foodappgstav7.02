package com.it10x.foodappgstav7_02.data.local.repository

import com.it10x.foodappgstav7_02.data.local.dao.CartDao
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(
    private val dao: CartDao
) {

    // ---------- OBSERVE CART (per table) ----------
    fun observeCart(tableId: String): Flow<List<PosCartEntity>> =
        dao.getCartForTable(tableId)

    // ---------- ADD ----------
    suspend fun addToCart(product: PosCartEntity) {
        val existing = dao.getByIdForTable(product.productId, product.tableId)
        if (existing == null) {
            dao.insert(product.copy(quantity = 1))
        } else {
            dao.update(existing.copy(quantity = existing.quantity + 1))
        }
    }

    // ---------- REMOVE SINGLE ITEM ----------
    suspend fun remove(item: PosCartEntity) {
        dao.delete(item)
    }

    // ---------- CLEAR CART (per table) ----------
    suspend fun clear(tableId: String) {
        dao.clearCart(tableId)
    }

    // ---------- DECREASE ----------
    suspend fun decrease(productId: String, tableId: String) {
        val existing = dao.getByIdForTable(productId, tableId) ?: return
        if (existing.quantity > 1) {
            dao.update(existing.copy(quantity = existing.quantity - 1))
        } else {
            dao.delete(existing)
        }
    }
}
