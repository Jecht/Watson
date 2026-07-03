package com.kapps.watson.core.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun platformOpenUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}