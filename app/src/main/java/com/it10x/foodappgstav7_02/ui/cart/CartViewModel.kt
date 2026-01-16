package com.it10x.foodappgstav7_02.ui.cart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.local.repository.CartRepository
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

    val sessionKey: StateFlow<String?> = sessionId
    // ---------- CART ----------
    val cart: StateFlow<List<PosCartEntity>> = sessionId
        .filterNotNull()
        .flatMapLatest { sid ->
            repository.observeCart(sid)
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
                // 🔑 auto-create fallback session
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
                )
            )
        }
    }


    fun increase(item: PosCartEntity) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.addToCart(
                item.copy(
                    sessionId = sessionId.value!!,
                    tableId = currentTableId.value
                )
            )
        }
    }

    fun decrease(productId: String) {
        if (!canMutateCart()) return

        val sid = sessionId.value ?: return

        viewModelScope.launch {
            repository.decrease(productId, sid)
        }
    }


    fun clear() {
        sessionId.value?.let { sid ->
            viewModelScope.launch {
                repository.clear(sid)
            }
        }
    }

    fun initSession(orderType: String, tableId: String? = null) {

        // ✅ Keep existing session if orderType/table didn't change
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == tableId
        ) {
            return
        }

        val sid = when (orderType) {
            "DINE_IN" -> tableId  // sessionKey = tableId
            "TAKEAWAY" -> "TAKEAWAY-${System.currentTimeMillis()}" // always new session
            "DELIVERY" -> "DELIVERY-${System.currentTimeMillis()}" // always new session
            else -> null
        }

        // ✅ save sessionKey
        savedStateHandle["orderType"] = orderType
        savedStateHandle["tableId"] = tableId
        savedStateHandle["sessionId"] = sid
    }





}
