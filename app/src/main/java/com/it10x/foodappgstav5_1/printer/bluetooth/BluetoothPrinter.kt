package com.it10x.foodappgstav5_1.printer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.OutputStream
import java.util.UUID

object BluetoothPrinter {

    private const val TAG = "PRINT_BT"

    private val SPP_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val mainHandler = Handler(Looper.getMainLooper())

    // =============================
    // TEST PRINT
    // =============================
    fun printTest(
        mac: String,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        printText(
            mac,
            """
            ****************************
                 TEST PRINT
            ****************************
            Printer Role : $roleLabel
            Connection   : BLUETOOTH
            Status       : OK
            ----------------------------
            
            
            """.trimIndent(),
            onResult
        )
    }

    // =============================
    // CORE PRINT (ORDER / AUTO)
    // =============================
    fun printText(
        mac: String,
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        Thread {
            var output: OutputStream? = null
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: throw IllegalStateException("Bluetooth not supported")

                if (!adapter.isEnabled) {
                    throw IllegalStateException("Bluetooth is OFF")
                }

                val device = adapter.getRemoteDevice(mac)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

                adapter.cancelDiscovery()
                socket.connect()

                output = socket.outputStream

                // ✅ ESC/POS INIT (ONCE)
                output.write(byteArrayOf(0x1B, 0x40))

                // ✅ IMPORTANT: convert LF → CRLF
                val safeText = text
                    .replace("\n", "\r\n")
                    .toByteArray(Charsets.US_ASCII)

                output.write(safeText)

                // ✅ FEED PAPER
                output.write(byteArrayOf(0x0A, 0x0A, 0x0A))

                output.flush()

                Thread.sleep(300)
                socket.close()

                mainHandler.post { onResult(true) }

            } catch (e: Exception) {
                Log.e(TAG, "Bluetooth print failed", e)
                mainHandler.post { onResult(false) }
            } finally {
                try { output?.close() } catch (_: Exception) {}
            }
        }.start()
    }
}
