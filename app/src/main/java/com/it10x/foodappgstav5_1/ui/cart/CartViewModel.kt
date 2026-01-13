package com.it10x.foodappgstav5_1.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.entities.PosCartEntity
import com.it10x.foodappgstav5_1.data.local.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository
) : ViewModel() {

    // Active table ID
    private val currentTableId = MutableStateFlow("T0")

    // Reactive cart per table (auto updates when table changes)
    val cart: StateFlow<List<PosCartEntity>> = currentTableId
        .flatMapLatest { tableId ->
            repository.observeCart(tableId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setTableId(id: String) {
        currentTableId.value = id
    }

    fun addToCart(product: PosCartEntity) {
        viewModelScope.launch {
            repository.addToCart(product.copy(tableId = currentTableId.value))
        }
    }

    fun increase(item: PosCartEntity) {
        viewModelScope.launch {
            repository.addToCart(item.copy(tableId = currentTableId.value))
        }
    }

    fun decrease(productId: String) {
        viewModelScope.launch {
            repository.decrease(productId, currentTableId.value)
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clear(currentTableId.value)
        }
    }
}
