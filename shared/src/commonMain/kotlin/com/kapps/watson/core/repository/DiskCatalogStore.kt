package com.kapps.watson.core.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [CatalogStore] backed by a single JSON file in the platform's app storage.
 *
 * All I/O is dispatched off the caller's thread and failures are contained: a missing or
 * unreadable cache simply behaves as "no cache yet", and a failed write is swallowed.
 */
internal class DiskCatalogStore : CatalogStore {

    override suspend fun read(): String? = withContext(Dispatchers.Default) {
        runCatching { readCatalogFile() }.getOrNull()
    }

    override suspend fun write(rawJson: String) {
        withContext(Dispatchers.Default) {
            runCatching { writeCatalogFile(rawJson) }
        }
    }
}
