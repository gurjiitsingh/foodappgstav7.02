package com.it10x.foodappgstav5_1.data.repository

import android.util.Log
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.it10x.foodappgstav5_1.data.models.OrderProductData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrdersRepository {

    private val db = FirebaseFirestore.getInstance()

    // -----------------------------
    // PAGINATION STATE
    // -----------------------------
    private val pageAnchors = mutableListOf<DocumentSnapshot>()
    private var lastDocument: DocumentSnapshot? = null

    fun resetPagination() {
        pageAnchors.clear()
        lastDocument = null
    }

    // -----------------------------
    // ORDER MASTER
    // -----------------------------
    suspend fun getFirstPage(limit: Long = 10): List<OrderMasterData> {
        val snapshot = db.collection("orderMaster")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        val docs = snapshot.documents
        if (docs.isNotEmpty()) {
            pageAnchors.add(docs.first())
            lastDocument = docs.last()
        }

        return docs.mapNotNull {
            it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
        }
    }

    suspend fun getNextPage(limit: Long = 10): List<OrderMasterData> {
        if (lastDocument == null) return emptyList()

        val snapshot = db.collection("orderMaster")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .startAfter(lastDocument!!)
            .limit(limit)
            .get()
            .await()

        val docs = snapshot.documents
        if (docs.isNotEmpty()) {
            pageAnchors.add(docs.first())
            lastDocument = docs.last()
        }

        return docs.mapNotNull {
            it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
        }
    }

    suspend fun getPrevPage(limit: Long = 10): List<OrderMasterData> {
        if (pageAnchors.size < 2) return emptyList()

        pageAnchors.removeLast()
        val prevAnchor = pageAnchors.last()

        val snapshot = db.collection("orderMaster")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .startAt(prevAnchor)
            .limit(limit)
            .get()
            .await()

        val docs = snapshot.documents
        if (docs.isNotEmpty()) {
            lastDocument = docs.last()
        }

        return docs.mapNotNull {
            it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
        }
    }

    // -----------------------------
    // ORDER PRODUCTS (ITEMS) ✅ NEW
    // -----------------------------
    suspend fun getOrderProducts(orderMasterId: String): List<OrderProductData> {

     //  Log.d("ORDER_REPO", "Fetching items for orderId=$orderMasterId")

        val snapshot = db.collection("orderProducts")
            .whereEqualTo("orderMasterId", orderMasterId)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(OrderProductData::class.java)?.copy(id = it.id)
        }
    }

    suspend fun markOrderAsPrinted(orderId: String) {
        db.collection("orderMaster")
            .document(orderId)
            .update("printed", true)
            .await()
    }


}
