package com.it10x.foodappgstav5_1.ui.pos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PosSessionViewModel : ViewModel() {

    private val _tableName = MutableStateFlow<String?>(null)
    val tableName = _tableName.asStateFlow()

    fun setTable(tableName: String) {
        _tableName.value = tableName
    }

    fun clearTable() {
        _tableName.value = null
    }
}
