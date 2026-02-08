package com.it10x.foodappgstav7_02.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_02.data.pos.dao.CartDao
import com.it10x.foodappgstav7_02.data.pos.dao.TableDao
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(
    private val dao: CartDao,
    private val tableDao: TableDao
) {

    // ---------- OBSERVE CART (per table) ----------

    fun observeCart(scopeKey: String): Flow<List<PosCartEntity>> =
        dao.getCartByScope(scopeKey)

    suspend fun isCartEmpty(tableNo: String): Boolean {
        val count = dao.getCartCount(tableNo)
       // Log.d("CART_DEBUG", "Cart count for table $tableNo = $count")
        return count == 0
    }
    // ---------- ADD ----------
    suspend fun addToCart(product: PosCartEntity, tableNo: String) {
        val existing = dao.getItemByIdForTable(product.productId, tableId = tableNo)
       // Log.d("TABLE_DEBUG", "item :${existing} table${tableNo}")
        if (existing == null) {
            dao.insert(product.copy(quantity = 1))

        } else {
            dao.update(existing.copy(quantity = existing.quantity + 1))
        }
        syncCartCount(tableNo)

    }

    suspend fun remove(productId: String, tableNo: String) {
      //  Log.d("TABLE_DEBUG", "item delete:${productId} table${tableNo}")
        dao.deleteItem(productId, tableNo)
        syncCartCount(tableNo)
    }
    suspend fun syncCartCount(tableNo: String) {
      //  Log.d("TABLE_DEBUG", "Cart to count and update table count")
        val count = dao.getCartCountForTable(tableNo) ?: 0
      //  Log.d("TABLE_DEBUG", "Cart pending count from DB = $count")
        tableDao.setCartCount(tableNo, count)
    }

    suspend fun getCartCountForTable(tableId: String): Int {
        return dao.getCartCount(tableId)
    }



    // ---------- REMOVE SINGLE ITEM ----------



    // ---------- CLEAR CART (per table) ----------
    suspend fun clear(tableId: String) {
        dao.clearCart(tableId)
    }


// ---------- DECREASE (SESSION BASED – FIXED) ----------
suspend fun decrease(productId: String, tableNo: String) {

    val existing = dao.getItemByIdForTable(productId, tableNo) ?: return
    Log.d(
        "TABLE_DEBUG",
        "DECREASE_CLICK (In CartRepository)  tableId=${tableNo} Product: ${existing}"
    )
    if (existing.quantity > 1) {
        dao.update(existing.copy(quantity = existing.quantity - 1))
    } else {
        dao.delete(existing)
    }
    syncCartCount(tableNo)
}




}
