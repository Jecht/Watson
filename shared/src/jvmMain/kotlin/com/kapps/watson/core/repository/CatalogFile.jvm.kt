package com.kapps.watson.core.repository

import java.io.File

/** Per-user app data directory; falls back to the temp dir if the home dir is unknown. */
private fun catalogDir(): File {
    val base = System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir")
    return File(base, ".watson")
}

internal actual fun readCatalogFile(): String? {
    val file = File(catalogDir(), CATALOG_FILE_NAME)
    return file.takeIf { it.exists() }?.readText()
}

internal actual fun writeCatalogFile(rawJson: String) {
    val dir = catalogDir()
    dir.mkdirs()
    File(dir, CATALOG_FILE_NAME).writeText(rawJson)
}
