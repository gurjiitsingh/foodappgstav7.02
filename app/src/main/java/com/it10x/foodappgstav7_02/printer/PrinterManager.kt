package com.it10x.foodappgstav7_02.printer

import android.content.Context
import android.util.Log
import com.it10x.foodappgstav7_02.data.PrinterConfig
import com.it10x.foodappgstav7_02.data.PrinterPreferences
import com.it10x.foodappgstav7_02.data.PrinterRole
import com.it10x.foodappgstav7_02.data.PrinterType
import com.it10x.foodappgstav7_02.printer.bluetooth.BluetoothPrinter
import com.it10x.foodappgstav7_02.printer.lan.LanPrinter
import com.it10x.foodappgstav7_02.printer.usb.USBPrinter

class PrinterManager(
    private val context: Context
) {

    private val prefs by lazy { PrinterPreferences(context) }
    fun appContext(): Context = context.applicationContext
    // --------------------------------
    // TEST PRINT (already OK)
    // --------------------------------
    fun printTest(
        config: PrinterConfig,
        onResult: (Boolean) -> Unit
    ) {
        val roleLabel = config.role.name

        when (config.type) {

            PrinterType.BLUETOOTH -> {
            //    Log.d("PRINT_BT", "Test BT address='${config.bluetoothAddress}'")
                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }
                BluetoothPrinter.printTest(
                    config.bluetoothAddress,
                    roleLabel,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }
                LanPrinter.printTest(
                    config.ip,
                    config.port,
                    roleLabel,
                    onResult
                )
            }



            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    onResult(false)
                    return
                }

                USBPrinter.printTest(
                    context = context,
                    device = device,
                    roleLabel = roleLabel,
                    onResult = onResult
                )
            }






            PrinterType.WIFI -> onResult(false)
        }
    }

    // --------------------------------
    // REAL PRINT (USED BY BUTTON + AUTO)
    // --------------------------------
    fun printTextNew(
        role: PrinterRole,
        order: PrintOrder, outletTitle: String = "FOOD APP",
        onResult: (Boolean) -> Unit = {}
    ) {

        Log.e("PRINT", "printer configured for role=$role")
        val config = prefs.getPrinterConfig(role)
        if (config == null) {
            Log.e("PRINT", "No printer configured for role=$role")
            onResult(false)
            return
        }

        // Determine page width from printer config
        val lineWidth = when (config.pageWidth) {  // assume you added 'pageWidth' in PrinterConfig
            48 -> 48      // 83mm printer
            else -> 32    // default 58mm printer
        }

        if(config.pageSize==MM_80){
            val receiptText = ReceiptFormatter.billing48(order, title = outletTitle)
        }else{
            val receiptText = ReceiptFormatter.billing(order, title = outletTitle)
        }

        //Log.d("PRINT", "Printing role=$role type=${config.type}")
        //  var  text1="kljkl"
        when (config.type) {

            PrinterType.BLUETOOTH -> {
                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }
                BluetoothPrinter.printText(
                    config.bluetoothAddress,
                    receiptText,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }
                LanPrinter.printText(
                    config.ip,
                    config.port,
                    receiptText,
                    onResult
                )
            }

            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    onResult(false)
                    return
                }
                USBPrinter.printText(
                    receiptText,
                    onResult
                )

//USBPrinter.printText(
//    context,
//    device,
//    text,
//    onResult
//)

            }

            PrinterType.WIFI -> onResult(false)
        }
    }

  fun printText(
    role: PrinterRole,
    text: String,
    onResult: (Boolean) -> Unit = {}
) {
        Log.e("PRINT", "printer configured for role=$role")
    val config = prefs.getPrinterConfig(role)
    if (config == null) {
        Log.e("PRINT", "No printer configured for role=$role")
        onResult(false)
        return
    }

    //Log.d("PRINT", "Printing role=$role type=${config.type}")
    //  var  text1="kljkl"
    when (config.type) {

        PrinterType.BLUETOOTH -> {
            if (config.bluetoothAddress.isBlank()) {
                onResult(false)
                return
            }
            BluetoothPrinter.printText(
                config.bluetoothAddress,
                text,
                onResult
            )
        }

        PrinterType.LAN -> {
            if (config.ip.isBlank()) {
                onResult(false)
                return
            }
            LanPrinter.printText(
                config.ip,
                config.port,
                text,
                onResult
            )
        }

        PrinterType.USB -> {
            val device = config.usbDevice ?: run {
                onResult(false)
                return
            }
            USBPrinter.printText(
                text,
       onResult
            )

//USBPrinter.printText(
//    context,
//    device,
//    text,
//    onResult
//)

        }

        PrinterType.WIFI -> onResult(false)
    }
}


    // --------------------------------
    // OPTIONAL
    // --------------------------------
    fun printTestForRole(
        configProvider: () -> PrinterConfig?,
        onResult: (Boolean) -> Unit
    ) {
        val config = configProvider()
        if (config == null) {
            onResult(false)
            return
        }
        printTest(config, onResult)
    }
}
