package com.it10x.foodappgstav5_1.data.repository

import android.util.Log
import com.it10x.foodappgstav5_1.data.models.OrderMasterData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class RealtimeOrdersRepository {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    fun startListening(
        onNewOrder: (OrderMasterData) -> Unit
    ) {
        listener = db.collection("orderMaster")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5) // only latest few orders
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Log.e("REALTIME_ORDER", "Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                for (change in snapshots.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val order = change.document
                            .toObject(OrderMasterData::class.java)
                            .copy(id = change.document.id)

                        Log.d(
                            "REALTIME_ORDER",
                            "New order detected: ${order.srno}"
                        )

                        onNewOrder(order)
                    }
                }
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }
}
