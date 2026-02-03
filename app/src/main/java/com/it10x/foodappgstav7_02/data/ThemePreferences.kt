package com.it10x.foodappgstav7_02.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore("theme_prefs")

class ThemePreferences(private val context: Context) {

    companion object {
        private val DARK_MODE = stringPreferencesKey("dark_mode")
        private val STYLE = stringPreferencesKey("style")
    }

//    val darkMode: Flow<String> =
//        context.themeDataStore.data.map { it[DARK_MODE] ?: "AUTO" }
val darkMode: Flow<String> =
    context.themeDataStore.data.map { it[DARK_MODE] ?: "DARK" }

    val style: Flow<String> =
        context.themeDataStore.data.map { it[STYLE] ?: "FAST_POS" }

    suspend fun setDarkMode(mode: String) {
        context.themeDataStore.edit { it[DARK_MODE] = mode }
    }

    suspend fun setStyle(style: String) {
        context.themeDataStore.edit { it[STYLE] = style }
    }
}
