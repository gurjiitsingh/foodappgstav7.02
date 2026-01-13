package com.it10x.foodappgstav5_1.printer.lan

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

object LanPrinter {

    private const val TAG = "LanPrinter"
    private const val TIMEOUT = 3000

    private val mainHandler = Handler(Looper.getMainLooper())

    fun printTest(
        ip: String,
        port: Int,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        val testText = """
        ****************************
             TEST PRINT
        ****************************
        Printer Role : $roleLabel
        Connection   : LAN
        IP Address   : $ip
        Port         : $port
        Status       : OK
        ----------------------------
        
        
    """.trimIndent()

        printText(
            ip = ip,
            port = port,
            text = testText,
            onResult = onResult
        )
    }



    fun printText(
        ip: String,
        port: Int,
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        Thread {
            var socket: Socket? = null
            var output: OutputStream? = null

            try {
                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), TIMEOUT)

                output = socket.getOutputStream()

                // ESC/POS init
                output.write(byteArrayOf(0x1B, 0x40))
                output.write(text.toByteArray(Charsets.UTF_8))
                output.write(byteArrayOf(0x0A, 0x0A, 0x0A))

                output.flush()

                mainHandler.post {
                    onResult(true)
                }

            } catch (e: Exception) {
                Log.e(TAG, "LAN print failed", e)
                mainHandler.post {
                    onResult(false)
                }
            } finally {
                try {
                    output?.close()
                    socket?.close()
                } catch (_: Exception) {}
            }
        }.start()
    }
}
