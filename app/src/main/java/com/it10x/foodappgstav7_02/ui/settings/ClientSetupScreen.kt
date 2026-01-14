package com.it10x.foodappgstav7_02.ui.settings

import android.os.Process
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.firebase.ClientIdStore


@Composable
fun ClientSetupScreen() {

    val context = LocalContext.current
    var clientId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "POS Activation",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID / License Key") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = clientId.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                ClientIdStore.save(context, clientId.trim())

                // 🔁 Full restart so Firebase re-initializes correctly
                Process.killProcess(Process.myPid())
            }
        ) {
            Text("Activate")
        }
    }
}
