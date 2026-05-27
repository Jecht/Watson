package com.kapps.watson.core.platform

import java.awt.Desktop
import java.net.URI

actual fun openUrlInBrowser(url: String) {
    runCatching {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
        }
    }
}