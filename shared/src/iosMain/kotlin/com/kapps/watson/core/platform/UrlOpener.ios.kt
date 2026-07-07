package com.kapps.watson.core.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun platformOpenUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    // Use the modern openURL:options:completionHandler: API: the single-argument openURL: has been
    // deprecated since iOS 10 and is a silent no-op on current iOS, so the browser never opened.
    UIApplication.sharedApplication.openURL(
        url = nsUrl,
        options = emptyMap<Any?, Any?>(),
        completionHandler = null,
    )
}
