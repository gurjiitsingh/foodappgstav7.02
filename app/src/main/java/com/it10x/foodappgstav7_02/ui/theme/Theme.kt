package com.it10x.foodappgstav7_02.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =====================================================
// POS BRAND COLORS
// =====================================================

// 🔶 Primary POS Orange
val PosOrange = Color(0xFFF97316)

// ---------- DARK COMBO 1 (Fast POS / Cafe) ----------
val PosDarkBg = Color(0xFF0F172A)     // OLED friendly dark slate
val PosCardBg = Color(0xFF1E293B)
val PosBorder = Color(0xFF334155)

// ---------- DARK COMBO 2 (Premium Restaurant) ----------
val DarkAltBg = Color(0xFF020617)     // near black
val DarkAltCard = Color(0xFF111827)
val OrangeAlt = Color(0xFFFB923C)

val onPrimary = Color(0xFF1A1A1A)
// ---------- TEXT ----------
val PosTextPrimary = Color(0xFFF8FAFC)
val PosTextSecondary = Color(0xFFCBD5E1)

// ---------- LIGHT ----------
val LightBg = Color(0xFFF1F5F9)   // soft slate
val LightCard = Color(0xFFE5E7EB) // light card gray

// ---------- STATUS COLORS ----------
val PosSuccess = Color(0xFF16A34A)
val PosWarning = Color(0xFFFACC15)
val PosError = Color(0xFFDC2626)

// ---------- LIGHT GREEN ----------
val PosGreen = Color(0xFF16A34A)

// ---------- WHITE BLUE ----------
val PosBlue = Color(0xFF2563EB)

// =====================================================
// PRO POS ACCENT SYSTEM
// =====================================================

data class PosAccentColors(
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val successButton: Color,
    val onSuccessButton: Color,
    val dangerButton: Color,
    val onDangerButton: Color
)

private val ProAccentDark = PosAccentColors(
    primaryButton = PosOrange,
    onPrimaryButton = Color(0xFF1A1A1A),

    successButton = PosSuccess,
    onSuccessButton = Color.White,

    dangerButton = PosError,
    onDangerButton = Color.White
)

private val ProAccentLight = PosAccentColors(
    primaryButton = PosBlue,
    onPrimaryButton = Color.White,

    successButton = PosGreen,
    onSuccessButton = Color.White,

    dangerButton = PosError,
    onDangerButton = Color.White
)
// =====================================================
// COLOR SCHEMES
// =====================================================

// Dark Combo 1
private val DarkScheme = darkColorScheme(
    primary = PosOrange,
    onPrimary = Color.Black, // ✅ dark text on orange

    background = PosDarkBg,
    onBackground = PosTextPrimary,

    surface = PosCardBg,
    onSurface = PosTextPrimary,

    outline = PosBorder,

    error = PosError,
    onError = Color.White
)


// Dark Combo 2 (Premium)
private val DarkAltScheme = darkColorScheme(
    primary = OrangeAlt,
    onPrimary = Color.White,

    background = DarkAltBg,
    onBackground = PosTextPrimary,

    surface = DarkAltCard,
    onSurface = PosTextPrimary,

    outline = PosBorder,

    error = PosError,
    onError = Color.White
)

// Light
private val LightScheme = lightColorScheme(
    primary = PosGreen,
    onPrimary = Color.White,

    background = LightBg,
    onBackground = Color.Black,

    surface = LightCard,
    onSurface = Color.Black,

    outline = Color(0xFFE5E7EB),

    error = PosError,
    onError = Color.White
)



// PURE WHITE THEME (no yellow tint)
private val WhiteScheme = lightColorScheme(
    primary = PosBlue,
    onPrimary = Color.White,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    outline = Color(0xFFE5E7EB),

    error = PosError,
    onError = Color.White
)




// =====================================================
// THEME WRAPPER
// =====================================================

enum class PosDarkStyle {
    FAST_POS,
    PREMIUM,
    PRO_POS // ✅ new
}

@Composable
fun FoodPosTheme(
    mode: String = "DARK",
    darkStyle: PosDarkStyle = PosDarkStyle.FAST_POS,
    content: @Composable () -> Unit
) {

    val useDark = mode == "DARK"

    val scheme = when (mode) {

        "WHITE" -> WhiteScheme
        "LIGHT" -> LightScheme

        "DARK" -> if (darkStyle == PosDarkStyle.PREMIUM)
            DarkAltScheme
        else
            DarkScheme

        else -> if (isSystemInDarkTheme()) DarkScheme else LightScheme
    }

    // ✅ PRO accent injection
    PosTheme.accent = when(darkStyle) {
        PosDarkStyle.PRO_POS -> if (useDark) ProAccentDark else ProAccentLight
        else -> ProAccentLight // or define legacy accent if needed
    }


    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )


}


// =====================================================
// GLOBAL POS THEME ACCESS
// =====================================================

object PosTheme {
    lateinit var accent: PosAccentColors
        internal set // now only code in this file can set it
}
