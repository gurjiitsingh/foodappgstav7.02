package com.it10x.foodappgstav5_1.ui.settings

import com.it10x.foodappgstav5_1.data.PrinterConfig
import com.it10x.foodappgstav5_1.data.PrinterRole

data class PrinterSettingsState(
    val printers: Map<PrinterRole, PrinterConfig> = emptyMap()
)
