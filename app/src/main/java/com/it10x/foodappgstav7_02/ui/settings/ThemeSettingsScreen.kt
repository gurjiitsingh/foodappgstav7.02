import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_02.viewmodel.ThemeViewModel
import com.it10x.foodappgstav7_02.ui.theme.PosDarkStyle

@Composable
fun ThemeSettingsScreen(vm: ThemeViewModel = viewModel()) {

    val darkMode by vm.darkMode.collectAsState()
    val style by vm.style.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Theme Settings", style = MaterialTheme.typography.titleLarge)

        Divider()

        Text("Dark Mode", style = MaterialTheme.typography.titleMedium)

        listOf("AUTO", "LIGHT", "WHITE", "DARK").forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = darkMode == mode,
                    onClick = { vm.setDarkMode(mode) }
                )
                Spacer(Modifier.width(8.dp))
                Text(mode)
            }
        }

        Divider()

        Text("Style", style = MaterialTheme.typography.titleMedium)

        // Add PRO_POS option
        listOf(PosDarkStyle.FAST_POS, PosDarkStyle.PREMIUM, PosDarkStyle.PRO_POS).forEach { s ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = style == s.name,
                    onClick = { vm.setStyle(s.name) }
                )
                Spacer(Modifier.width(8.dp))
                Text(s.name)
            }
        }
    }
}
