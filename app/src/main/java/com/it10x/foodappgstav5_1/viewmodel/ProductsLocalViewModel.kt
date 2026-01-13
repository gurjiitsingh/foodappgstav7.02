package com.it10x.foodappgstav5_1.data.local.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.dao.ProductDao
import com.it10x.foodappgstav5_1.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProductsLocalViewModel(
    dao: ProductDao
) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> =
        dao.getAll()
            .map { it.sortedBy { p -> p.name } }   // optional
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}
