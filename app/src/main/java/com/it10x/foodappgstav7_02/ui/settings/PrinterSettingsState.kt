package com.it10x.foodappgstav7_02.ui.settings

import com.it10x.foodappgstav7_02.data.PrinterConfig
import com.it10x.foodappgstav7_02.data.PrinterRole

data class PrinterSettingsState(
    val printers: Map<PrinterRole, PrinterConfig> = emptyMap()
)
