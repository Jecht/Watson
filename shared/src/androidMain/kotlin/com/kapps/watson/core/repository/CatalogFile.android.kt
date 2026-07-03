package com.kapps.watson.core.repository

import android.content.Context
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

private fun catalogFile(): File =
    File(getKoin().get<Context>().filesDir, CATALOG_FILE_NAME)

internal actual fun readCatalogFile(): String? =
    catalogFile().takeIf { it.exists() }?.readText()

internal actual fun writeCatalogFile(rawJson: String) {
    catalogFile().writeText(rawJson)
}
