package com.it10x.foodappgstav5_1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ✅ Tailwind green-600
val Green600 = Color(0xFF16A34A)
val White = Color(0xFFFFFFFF)

// Optional other colors
val DarkGray = Color(0xFF121212)
val LightGray = Color(0xFFF5F5F5)

private val DarkColorScheme = darkColorScheme(
    primary = Green600,
    onPrimary = White,
    secondary = Green600,
    onSecondary = White,
    surface = DarkGray,
    onSurface = White,
    error = Color(0xFFCF6679),
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = White,
    secondary = Green600,
    onSecondary = White,
    surface = LightGray,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = White
)


@Composable
fun foodappgstaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ❌ set false
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


