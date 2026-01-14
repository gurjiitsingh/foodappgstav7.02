package com.it10x.foodappgstav7_02.ui.cart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.local.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.local.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CartUiEvent {
    object TableRequired : CartUiEvent()
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
    // ---------- CART ----------
    val cart: StateFlow<List<PosCartEntity>> = currentTableId
        .filterNotNull()
        .flatMapLatest { tableId ->
            repository.observeCart(tableId)
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
    private fun canMutateCart(): Boolean {
        return when (currentOrderType.value) {
            "DINE_IN" -> !currentTableId.value.isNullOrBlank()
            else -> true // TAKEAWAY / DELIVERY always allowed
        }
    }

    // ---------- MUTATIONS ----------
    fun addToCart(product: PosCartEntity) {
          viewModelScope.launch {
            if (!canMutateCart()) {
                _uiEvent.emit(CartUiEvent.TableRequired)
                return@launch
            }

            repository.addToCart(
                product.copy(tableId = currentTableId.value!!)
            )
        }
    }

    fun increase(item: PosCartEntity) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.addToCart(
                item.copy(tableId = currentTableId.value!!)
            )
        }
    }

    fun decrease(productId: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.decrease(productId, currentTableId.value!!)
        }
    }

    fun clear() {
        currentTableId.value?.let {
            viewModelScope.launch {
                repository.clear(it)
            }
        }
    }
}
