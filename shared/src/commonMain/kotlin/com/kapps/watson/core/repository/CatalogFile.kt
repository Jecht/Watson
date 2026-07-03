package com.kapps.watson.core.repository

/** Name of the on-disk file holding the cached catalog, shared by every platform actual. */
internal const val CATALOG_FILE_NAME = "sherlock_catalog.json"

/** Reads the cached catalog file, or returns null when it is absent or unreadable. */
internal expect fun readCatalogFile(): String?

/** Writes [rawJson] to the cached catalog file, creating parent directories as needed. */
internal expect fun writeCatalogFile(rawJson: String)
