package com.kapps.watson.core.repository

/**
 * Persists the raw Sherlock catalog JSON on the device so the app can start from a
 * last-known-good copy when the remote endpoint is unreachable, slow, or serving a
 * broken payload. Backed by a single file per platform.
 */
internal interface CatalogStore {

    /** Returns the last persisted raw catalog JSON, or null if nothing was stored yet. */
    suspend fun read(): String?

    /** Persists [rawJson] as the new last-known-good catalog. Best-effort: failures are swallowed. */
    suspend fun write(rawJson: String)
}
