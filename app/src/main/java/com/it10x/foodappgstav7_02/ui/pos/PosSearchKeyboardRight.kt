package com.it10x.foodappgstav7_02.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PosSearchKeyboardRight(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp), // ⬅️ less padding to pull up
        verticalArrangement = Arrangement.Top, // ⬅️ move grid toward top
    ) {

        val rows = listOf(
            listOf("1", "2", "3", "4", "5"),
            listOf("6", "7", "8", "9", "0"),
            listOf("⌫", "CLEAR", "SPACE", "OK", "A"),
            listOf("B", "C", "D", "E", "F"),
            listOf("G", "H", "I", "J", "K"),
            listOf("L", "M", "N", "O", "P"),
            listOf("Q", "R", "S", "T", "U"),
            listOf("V", "W", "X", "Y", "Z"),
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    KeyButtonStyled(
                        label = key,
                        weight = if (key == "CLEAR") 2f else 1f,
                        height = 38.dp // ⬅️ smaller button height
                    ) {
                        when (key) {
                            "⌫" -> onBackspace()
                            "CLEAR" -> onClear()
                            "OK" -> onClose()
                            "SPACE" -> onKeyPress(" ")
                            else -> onKeyPress(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButtonStyled(
    label: String,
    weight: Float = 1f,
    height: Dp = 44.dp, // ⬅️ parameterized height
    onClick: () -> Unit
) {
    val color = when (label) {
        "OK" -> Color(0xFF81C784)     // ✅ soft green
        "CLEAR" -> Color(0xFFFFCDD2)  // ❌ soft red
        "⌫" -> Color(0xFFBBDEFB)      // 🔙 soft blue
        "SPACE" -> Color(0xFFFFF9C4)  // 🟨 soft yellow
        else -> Color(0xFFE0E0E0)     // default light gray
    }

    val textColor = when (label) {
        "OK" -> Color(0xFF1B5E20)
        "CLEAR" -> Color(0xFFB71C1C)
        "⌫" -> Color(0xFF0D47A1)
        "SPACE" -> Color(0xFF5D4037)
        else -> Color.Black
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            //.weight(weight)
            .height(height),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, color = textColor)
    }
}
