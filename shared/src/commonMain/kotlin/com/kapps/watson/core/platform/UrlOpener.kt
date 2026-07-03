package com.kapps.watson.core.platform

/**
 * Opens [url] in the platform's default web browser.
 *
 * Each platform provides its own implementation via an `actual` declaration:
 * - Android uses Intent.ACTION_VIEW
 * - Desktop uses Desktop.getDesktop().browse()
 * - iOS uses UIApplication.openURL()
 *
 * On failure (no browser available, invalid URL, ...), the actual silently
 * does nothing rather than crashing the app.
 */
internal expect fun platformOpenUrl(url: String)

/**
 * Opens [url] in the platform browser, but only when it is an http/https URL.
 *
 * The URL is built from the remotely-fetched Sherlock catalog (`url` pattern + username),
 * so restricting the scheme prevents a hostile or malformed entry from launching
 * `file:`, `javascript:`, `intent:` or other app-scheme URIs through the system opener.
 * Anything else is silently ignored.
 */
fun openUrlInBrowser(url: String) {
    if (url.hasWebScheme()) {
        platformOpenUrl(url)
    }
}

/**
 * True when [this] starts with an `http`/`https` scheme (case-insensitive).
 * The scheme is the substring up to the first `:`; a missing or leading colon is rejected.
 */
private fun String.hasWebScheme(): Boolean {
    val trimmed = trimStart()
    val colonIndex = trimmed.indexOf(':')
    if (colonIndex <= 0) return false
    val scheme = trimmed.substring(0, colonIndex).lowercase()
    return scheme == "http" || scheme == "https"
}
