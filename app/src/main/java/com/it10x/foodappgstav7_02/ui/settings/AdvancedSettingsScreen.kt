package com.it10x.foodappgstav7_02.ui.settings

import android.os.Process
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_02.firebase.ClientIdStore

private const val ADMIN_PASSWORD = "gsta123456"

@Composable
fun AdvancedSettingsScreen() {

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Advanced Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Divider()

        Text(
            text = "Danger Zone",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                password = ""
                error = null
                showDialog = true
            }
        ) {
            Text("Change Client Setup")
        }
    }

    // 🔐 PASSWORD DIALOG
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Action") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter admin password to continue")

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = error != null
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (password == ADMIN_PASSWORD) {
                        ClientIdStore.clear(context)
                        Process.killProcess(Process.myPid())
                    } else {
                        error = "Invalid password"
                    }
                }) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
