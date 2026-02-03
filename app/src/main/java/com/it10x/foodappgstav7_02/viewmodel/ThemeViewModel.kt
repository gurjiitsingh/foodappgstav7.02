package com.it10x.foodappgstav7_02.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_02.data.ThemePreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = ThemePreferences(app)

    val darkMode = prefs.darkMode.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "DARK" // ⭐ default dark
    )

    val style = prefs.style.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "FAST_POS"
    )

    fun setDarkMode(mode: String) = viewModelScope.launch {
        prefs.setDarkMode(mode)
    }

    fun setStyle(style: String) = viewModelScope.launch {
        prefs.setStyle(style)
    }
}
