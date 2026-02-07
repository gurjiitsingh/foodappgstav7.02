package com.it10x.foodappgstav7_02.ui.cart

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_02.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_02.domain.usecase.TableReleaseUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CartUiEvent {
    object SessionRequired : CartUiEvent()
    object TableRequired : CartUiEvent()   // ✅ ADD THIS
}

class CartViewModel(
    private val repository: CartRepository,
    private val tableReleaseUseCase: TableReleaseUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


// TO CHECK INIT DATA
//    init {
//        Log.d(
//            "CART_VM",
//            "CartViewModel CREATED hash=${this.hashCode()}"
//        )
//    }
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

//    Log.d(
//        "CART_DEBUG",
//        "canMutateCart (In CartViewModel:canMutateCart)  currentOrderType.value=${currentOrderType.value} currentTableId.value=${currentTableId.value} "
//    )


        return when (currentOrderType.value) {
            "DINE_IN" -> !currentTableId.value.isNullOrBlank()
            "TAKEAWAY" -> !currentTableId.value.isNullOrBlank()
            "DELIVERY" -> !currentTableId.value.isNullOrBlank()
            else -> true //NOT :-- TAKEAWAY / DELIVERY always allowed
        }
    }

    // ---------- MUTATIONS ----------
    fun addToCart(product: PosCartEntity) {

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


    fun decrease(productId: String, tableNo: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.decrease(productId, tableNo)

            // ✅ single source of truth
            tableReleaseUseCase.releaseIfOrderingAndCartEmpty(tableNo)
        }
    }



    fun removeFromCart(productId: String, tableNo: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.remove(productId, tableNo)
            tableReleaseUseCase.releaseIfOrderingAndCartEmpty(tableNo)
        }
    }


    fun initSession(orderType: String, tableId: String? = null) {

        val resolvedTableId = when (orderType) {
            "DINE_IN" -> tableId
            "TAKEAWAY" -> tableId
            "DELIVERY" -> tableId
            else -> null
        }

        if (resolvedTableId.isNullOrBlank()) return

        // ✅ PREVENT DUPLICATE SESSION CREATION
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == resolvedTableId
        ) {
           // Log.d("CART_DEBUG", "initSession skipped (already active)")
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
            "TAKEAWAY" -> currentTableId.value
            "DELIVERY" -> currentTableId.value
            else -> null
        }
    }




}
