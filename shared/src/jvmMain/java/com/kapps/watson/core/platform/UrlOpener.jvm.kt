package com.kapps.watson.core.platform

import java.awt.Desktop
import java.net.URI

internal actual fun platformOpenUrl(url: String) {
    runCatching {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
        }
    }
}