package com.kapps.watson.core.repository

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/** Absolute path to the cached catalog inside the app's Documents directory, or null. */
private fun catalogFilePath(): String? {
    val documents = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).firstOrNull() as? String ?: return null
    return "$documents/$CATALOG_FILE_NAME"
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun readCatalogFile(): String? {
    val path = catalogFilePath() ?: return null
    return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun writeCatalogFile(rawJson: String) {
    val path = catalogFilePath() ?: return
    val bytes = rawJson.encodeToByteArray()
    if (bytes.isEmpty()) return
    val data = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    data.writeToFile(path, atomically = true)
}
