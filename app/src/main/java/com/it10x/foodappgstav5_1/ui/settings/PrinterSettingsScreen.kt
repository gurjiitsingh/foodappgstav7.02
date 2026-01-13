package com.it10x.foodappgstav5_1.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav5_1.data.PrinterPreferences
import com.it10x.foodappgstav5_1.data.PrinterRole
import com.it10x.foodappgstav5_1.data.PrinterType
import com.it10x.foodappgstav5_1.viewmodel.PrinterSettingsViewModel

@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel,
    prefs: PrinterPreferences,
    role: PrinterRole,                  // ✅ REQUIRED
    onSave: () -> Unit,
    onBack: () -> Unit,
    onBluetoothSelected: (PrinterSettingsViewModel) -> Unit,
    onUSBSelected: () -> Unit,
    onLanSelected: () -> Unit
) {
    val context = LocalContext.current

    val printerType by viewModel.printerTypeMap[role]!!.collectAsState()
    val btPrinterName by viewModel.btNameMap[role]!!.collectAsState()

    val usbPrinterName = prefs.getUSBPrinterName(role)
    val usbPrinterId = prefs.getUSBPrinterId(role)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Printer Settings - ${role.name}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(20.dp))

        // ---------- PRINTER TYPE ----------

        PrinterType.values().forEach { type ->
            val isSelected = printerType == type

            if (isSelected) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = {
                        viewModel.updatePrinterType(role, type)

                        when (type) {
                            PrinterType.BLUETOOTH ->
                                onBluetoothSelected(viewModel)

                            PrinterType.USB ->
                                onUSBSelected()

                            PrinterType.LAN ->
                                onLanSelected()

                            else -> {}
                        }
                    }
                ) {
                    Text(type.name)
                }
            } else {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = {
                        viewModel.updatePrinterType(role, type)

                        when (type) {
                            PrinterType.BLUETOOTH ->
                                onBluetoothSelected(viewModel)   // <-- FIXED HERE

                            PrinterType.USB ->
                                onUSBSelected()

                            PrinterType.LAN ->
                                onLanSelected()

                            else -> {}
                        }
                    }
                ) {
                    Text(type.name)
                }
            }
        }





        Spacer(Modifier.height(16.dp))

        // ---------- BLUETOOTH INFO ----------
        if (printerType == PrinterType.BLUETOOTH && btPrinterName.isNotBlank()) {
            Text("Bluetooth Printer: $btPrinterName")
        }

        // ---------- USB INFO ----------
        if (printerType == PrinterType.USB && usbPrinterName.isNotBlank()) {
            Text("USB Printer: $usbPrinterName (ID: $usbPrinterId)")
        }

        // ---------- LAN INFO ----------
        if (printerType == PrinterType.LAN) {
            val ip = prefs.getLanPrinterIP(role)
            val port = prefs.getLanPrinterPort(role)

            if (ip.isNotBlank()) {
                Text("LAN Printer: $ip:$port")
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- TEST PRINT ----------
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.testPrint(role) { success ->
                    Toast.makeText(
                        context,
                        if (success) "Test print successful"
                        else "Test print failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        ) {
            Text("Test Print")
        }

        Spacer(Modifier.height(16.dp))

        // ---------- SAVE ----------
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSave
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}
