package com.kapps.watson.core.platform

/**
 * Opens [url] in the platform's default web browser.
 *
 * Each platform provides its own implementation via an `actual` declaration:
 * - Android uses Intent.ACTION_VIEW
 * - Desktop uses Desktop.getDesktop().browse()
 * - iOS uses UIApplication.openURL()
 *
 * On failure (no browser available, invalid URL, ...), this function silently
 * does nothing rather than crashing the app.
 */
expect fun openUrlInBrowser(url: String)