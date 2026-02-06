package com.it10x.foodappgstav7_02.ui.cart

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CartUiEvent {
    object SessionRequired : CartUiEvent()
    object TableRequired : CartUiEvent()   // ✅ ADD THIS
}

class CartViewModel(
    private val repository: CartRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


// TO CHECK INIT DATA
    init {
        Log.d(
            "CART_VM",
            "CartViewModel CREATED hash=${this.hashCode()}"
        )
    }
    // ---------- ORDER CONTEXT ----------
    private val currentTableId =
        savedStateHandle.getStateFlow<String?>("tableId", null)

    private val currentOrderType =
        savedStateHandle.getStateFlow("orderType", "DINE_IN")

    private val _uiEvent = MutableSharedFlow<CartUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    // ---------- SESSION ----------
    private val sessionId =
        savedStateHandle.getStateFlow<String?>("sessionId", null)

    val  sessionKey: StateFlow<String?> = sessionId
    // ---------- CART ----------
    val cart: StateFlow<List<PosCartEntity>> =
        combine(currentOrderType, currentTableId) { _, _ ->
            cartScopeKey()
        }
            .filterNotNull()
            .flatMapLatest { scopeKey ->
                repository.observeCart(scopeKey)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



    // ---------- SETTERS ----------
    fun setTableId(id: String?) {
        savedStateHandle["tableId"] = id
    }

    fun setOrderType(type: String) {
        savedStateHandle["orderType"] = type
    }

    // ---------- POS ORDER GUARD ----------


//    private fun canMutateCart(): Boolean {
//        return !sessionId.value.isNullOrBlank()
//    }
    private fun canMutateCart(): Boolean {

    Log.d(
        "CART_DEBUG",
        "canMutateCart (In CartViewModel:canMutateCart)  currentOrderType.value=${currentOrderType.value} currentTableId.value=${currentTableId.value} "
    )


        return when (currentOrderType.value) {
            "DINE_IN" -> !currentTableId.value.isNullOrBlank()
            else -> true // TAKEAWAY / DELIVERY always allowed
        }
    }

    // ---------- MUTATIONS ----------
    fun addToCart(product: PosCartEntity) {

//        Log.d(
//            "POS_DEBUG",
//            "ADD_CLICK orderType=${posSessionViewModel.orderType} tableId=${posSessionViewModel.tableId.value}"
//        )
        viewModelScope.launch {

            if (sessionId.value.isNullOrBlank()) {
                _uiEvent.emit(CartUiEvent.SessionRequired)
                initSession(currentOrderType.value, currentTableId.value)
            }

            if (!canMutateCart()) {
                _uiEvent.emit(CartUiEvent.TableRequired)
                return@launch
            }

            repository.addToCart(

                product.copy(
                    sessionId = sessionId.value!!,
                    tableId = currentTableId.value
                ),
                tableNo =  currentTableId.value!!,
            )
        }
    }



    fun increase(item: PosCartEntity) {
        if (!canMutateCart()) return

        val sid = sessionId.value ?: return

        viewModelScope.launch {
            repository.addToCart(
                item.copy(
                    sessionId = sid,
                    tableId = currentTableId.value
                ),
                tableNo = currentTableId.value!!,
            )
        }
    }

    fun decrease(productId: String, tableNo: String) {


        if (!canMutateCart()) return

        val sid = sessionId.value ?: return

        viewModelScope.launch {
            repository.decrease(productId, tableNo)
        }
    }

    fun removeFromCart(productId: String,tableNo : String) {
        if (!canMutateCart()) return

        val sid = sessionId.value ?: return

        viewModelScope.launch {
            repository.remove(productId, tableNo)  // <-- repository should have a remove function
        }
    }

    fun clear() {
        val sid = sessionId.value ?: return

        viewModelScope.launch {
            // 🧹 clear cart items
            repository.clear(sid)

            // 🔑 reset session
            savedStateHandle["sessionId"] = null
        }
    }
    fun initSession_OLD(orderType: String, tableId: String? = null) {

        Log.d(
            "CART_DEBUG",
            "initSession (In CartViewModel) orderType=${orderType}  tableId=${tableId}"
        )

        // ✅ Keep existing session if orderType/table didn't change
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == tableId
        ) {
            return
        }

        val sid = when (orderType) {
            "DINE_IN" -> {
                if (tableId.isNullOrBlank()) return
                "DINEIN-${tableId}-${System.currentTimeMillis()}"
            }
            "TAKEAWAY" -> "TAKEAWAY-${System.currentTimeMillis()}"
            "DELIVERY" -> "DELIVERY-${System.currentTimeMillis()}"
            else -> return
        }

        // ✅ save sessionKey
        savedStateHandle["orderType"] = orderType
        savedStateHandle["tableId"] = tableId
        savedStateHandle["sessionId"] = sid
    }
    fun initSession(orderType: String, tableId: String? = null) {

        val resolvedTableId = when (orderType) {
            "DINE_IN" -> tableId
            "TAKEAWAY" -> "TAKEAWAY"
            "DELIVERY" -> "DELIVERY"
            else -> null
        }

        if (resolvedTableId.isNullOrBlank()) return

        // ✅ PREVENT DUPLICATE SESSION CREATION
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == resolvedTableId
        ) {
            Log.d("CART_DEBUG", "initSession skipped (already active)")
            return
        }

        val sid = "$orderType-$resolvedTableId-${System.currentTimeMillis()}"


        savedStateHandle["orderType"] = orderType
        savedStateHandle["tableId"] = resolvedTableId
        savedStateHandle["sessionId"] = sid
    }



    private fun cartScopeKey(): String? {
        return when (currentOrderType.value) {
            "DINE_IN" -> currentTableId.value
            "TAKEAWAY" -> "TAKEAWAY"
            "DELIVERY" -> "DELIVERY"
            else -> null
        }
    }




}
