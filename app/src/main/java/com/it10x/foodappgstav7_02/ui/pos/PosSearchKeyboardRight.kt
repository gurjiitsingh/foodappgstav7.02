package com.it10x.foodappgstav7_02.ui.pos



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        val rows = listOf(
            listOf("1","2","3","4","5"),
            listOf("6","7","8","9","0"),
            listOf("A","B","C","D","E"),
            listOf("F","G","H","I","J"),
            listOf("K","L","M","N","O"),
            listOf("P","Q","R","S","T"),
            listOf("U","V","W","X","Y"),
            listOf("Z","⌫","CLEAR","SPACE","OK"),

        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    KeyButton(
                        label = key,
                        weight = if (key == "CLEAR") 2f else 1f
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
fun KeyButton(
    label: String,
    weight: Float = 1f,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
           // .weight(weight)     // 🔥 REQUIRED
            .height(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label)
    }
}
