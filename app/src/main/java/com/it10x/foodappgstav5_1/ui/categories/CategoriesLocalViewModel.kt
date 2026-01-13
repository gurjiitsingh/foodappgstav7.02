package com.it10x.foodappgstav5_1.data.local.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav5_1.data.local.dao.CategoryDao
import com.it10x.foodappgstav5_1.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CategoriesLocalViewModel(
    dao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        dao.getAll()
            .map { it.sortedBy { c -> c.name } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}

class CategoriesLocalViewModelFactory(
    private val dao: CategoryDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesLocalViewModel(dao) as T
    }
}
