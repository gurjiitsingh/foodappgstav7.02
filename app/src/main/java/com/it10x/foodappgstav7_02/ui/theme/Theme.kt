package com.it10x.foodappgstav7_02.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =====================================================
// POS BRAND COLORS (LOCKED DEFAULTS)
// =====================================================

val PosOrange = Color(0xFFF97316)

val PosDarkBg = Color(0xFF0F172A)
val PosCardBg = Color(0xFF1E293B)
val PosBorder = Color(0xFF334155)

val DarkAltBg = Color(0xFF020617)
val DarkAltCard = Color(0xFF111827)
val OrangeAlt = Color(0xFFFB923C)

val PosTextPrimary = Color(0xFFF8FAFC)
val PosTextSecondary = Color(0xFFCBD5E1)

val LightBg = Color(0xFFF1F5F9)
val LightCard = Color(0xFFE5E7EB)

val PosSuccess = Color(0xFF16A34A)
val PosWarning = Color(0xFFFACC15)
val PosError = Color(0xFFDC2626)

val PosGreen = Color(0xFF16A34A)
val PosBlue = Color(0xFF2563EB)

// =====================================================
// ACCENT SYSTEM
// =====================================================

data class PosAccentColors(
    val primaryButton: Color,
    val onPrimaryButton: Color,

    val successButton: Color,
    val onSuccessButton: Color,

    val warningButton: Color,
    val onWarningButton: Color,

    val dangerButton: Color,
    val onDangerButton: Color,

    val cartAdd: Color,
    val onCartAdd: Color,
    val cartRemove: Color,
    val onCartRemove: Color
)

// ---------- FAST POS (current look locked) ----------
private val FastAccent = PosAccentColors(
    primaryButton = PosOrange,
    onPrimaryButton = Color.Black,

    successButton = PosSuccess,
    onSuccessButton = Color.White,

    warningButton = PosWarning,
    onWarningButton = Color(0xFF1A1A1A),

    dangerButton = PosError,
    onDangerButton = Color.White,

    cartAdd = Color(0xFF16A34A),
    onCartAdd = Color.White,

    cartRemove = Color(0xFFDC2626),
    onCartRemove = Color.White
)

// ---------- PREMIUM (same for now — editable later) ----------
private val PremiumAccent = FastAccent

// ---------- PRO POS (same for now — editable later) ----------
private val ProAccent = FastAccent

// =====================================================
// COLOR SCHEMES
// =====================================================

private val DarkScheme = darkColorScheme(
    primary = PosOrange,
    onPrimary = Color.Black,
    background = PosDarkBg,
    onBackground = PosTextPrimary,
    surface = PosCardBg,
    onSurface = PosTextPrimary,
    outline = PosBorder,
    error = PosError,
    onError = Color.White
)

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
    PRO_POS
}

@Composable
fun FoodPosTheme(
    mode: String = "DARK",
    darkStyle: PosDarkStyle = PosDarkStyle.FAST_POS,
    content: @Composable () -> Unit
) {

    val scheme = when (mode) {
        "WHITE" -> WhiteScheme
        "LIGHT" -> LightScheme
        "DARK" -> if (darkStyle == PosDarkStyle.PREMIUM) DarkAltScheme else DarkScheme
        else -> if (isSystemInDarkTheme()) DarkScheme else LightScheme
    }

    PosTheme.accent = when (darkStyle) {
        PosDarkStyle.FAST_POS -> FastAccent
        PosDarkStyle.PREMIUM -> PremiumAccent
        PosDarkStyle.PRO_POS -> ProAccent
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )
}

// =====================================================
// GLOBAL ACCESS
// =====================================================

object PosTheme {
    lateinit var accent: PosAccentColors
        internal set
}
