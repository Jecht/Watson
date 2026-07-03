package com.kapps.watson.core.repository

import com.kapps.watson.core.model.SiteInfo
import com.kapps.watson.core.network.ExclusionsService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Repository in charge of fetching and caching the Sherlock site catalog.
 *
 * The catalog is hosted at https://data.sherlockproject.xyz and updated by the Sherlock community.
 * Because that endpoint is a hard external dependency, the catalog is resolved offline-first with a
 * stale-while-revalidate strategy: in-memory cache → on-disk cache (revalidated in the background)
 * → network. A network or parse failure never crashes the app; it degrades to the last-known-good
 * copy, down to an empty map only on a cold, never-online start.
 */
internal class SitesRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val exclusionsService: ExclusionsService,
    private val catalogStore: CatalogStore,
) : SitesRepository {

    private val catalogUrl = "https://data.sherlockproject.xyz"

    private var cachedSites: Map<String, SiteInfo>? = null
    private var cachedExclusions: Set<String>? = null

    /** Detached scope for background revalidation; never joined by callers. */
    private val refreshScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun loadSites(
        forceRefresh: Boolean,
        honorExclusions: Boolean,
    ): Map<String, SiteInfo> {
        val allSites = loadAllSites(forceRefresh = forceRefresh)
        if (honorExclusions.not()) return allSites

        val exclusions = loadExclusions(forceRefresh = forceRefresh)
        return allSites.filterKeys { siteName -> siteName !in exclusions }
    }

    private suspend fun loadAllSites(forceRefresh: Boolean): Map<String, SiteInfo> {
        if (!forceRefresh) {
            cachedSites?.let { return it }

            parseDiskCatalog()?.let { fromDisk ->
                cachedSites = fromDisk
                // Serve the cached copy immediately, refresh in the background.
                triggerBackgroundRefresh()
                return fromDisk
            }
        }

        // No usable cache (cold start or forced refresh): go to the network, but degrade
        // gracefully to whatever last-known-good copy exists rather than failing the scan.
        return runCatching { fetchAndPersistCatalog() }
            .getOrNull()
            ?: cachedSites
            ?: parseDiskCatalog()?.also { cachedSites = it }
            ?: emptyMap()
    }

    /** Downloads, validates, caches (memory + disk) and returns the remote catalog. */
    private suspend fun fetchAndPersistCatalog(): Map<String, SiteInfo> = withContext(Dispatchers.Default) {
        val rawCatalog = httpClient.get(catalogUrl).bodyAsText()
        val parsed = parseCatalog(rawCatalog)
        // Schema guard: never let an empty/degenerate payload replace a good cache.
        check(parsed.isNotEmpty()) { "Remote catalog parsed to an empty map" }

        cachedSites = parsed
        catalogStore.write(rawCatalog)
        parsed
    }

    /** Reads and parses the on-disk catalog, or null when absent or unparseable. */
    private suspend fun parseDiskCatalog(): Map<String, SiteInfo>? {
        val raw = catalogStore.read() ?: return null
        return runCatching { parseCatalog(raw) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun triggerBackgroundRefresh() {
        refreshScope.launch {
            runCatching { fetchAndPersistCatalog() }
        }
    }

    private suspend fun loadExclusions(forceRefresh: Boolean): Set<String> {
        cachedExclusions?.takeUnless { forceRefresh }?.let { return it }
        val exclusions = exclusionsService.loadExclusions()
        cachedExclusions = exclusions
        return exclusions
    }

    private fun parseCatalog(rawJson: String): Map<String, SiteInfo> {
        val rootObject: JsonObject = json
            .parseToJsonElement(rawJson)
            .jsonObject

        return rootObject
            .filterKeys { key -> key != "\$schema" }
            .mapValues { (_, element) ->
                json.decodeFromJsonElement(SiteInfo.serializer(), element)
            }
    }
}
