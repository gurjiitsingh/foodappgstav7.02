package com.it10x.foodappgstav7_02.data.pos.repository

import com.it10x.foodappgstav7_02.data.pos.dao.CartDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(
    private val dao: CartDao
) {

    // ---------- OBSERVE CART (per table) ----------
    fun observeCart(sessionId: String): Flow<List<PosCartEntity>> =
        dao.getCartForSession(sessionId)





    // ---------- ADD ----------
    suspend fun addToCart(product: PosCartEntity) {
        val existing = dao.getByIdForSession(product.productId, product.sessionId)
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


// ---------- DECREASE (SESSION BASED – FIXED) ----------
suspend fun decrease(productId: String, sessionId: String) {
    val existing = dao.getByIdForSession(productId, sessionId) ?: return
    if (existing.quantity > 1) {
        dao.update(existing.copy(quantity = existing.quantity - 1))
    } else {
        dao.delete(existing)
    }
}


}
