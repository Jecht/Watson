package com.kapps.watson.core.repository

import com.kapps.watson.core.model.SiteInfo
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Repository in charge of fetching and caching the Sherlock site catalog.
 *
 * The catalog is hosted at https://data.sherlockproject.xyz and updated by the Sherlock community.
 * We download it on demand and parse it into a Map<String, SiteInfo> where the key is the site name
 * (e.g. "GitHub") and the value is its configuration.
 */
internal class SitesRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : SitesRepository {

    /**
     * URL of the canonical Sherlock site catalog.
     * Using the upstream source rather than a forked copy means we benefit from
     * community-maintained fixes (new sites, removed false positives, ...) for free.
     */
    private val catalogUrl = "https://data.sherlockproject.xyz"

    /** In-memory cache. Populated on the first call to [loadSites]. */
    private var cachedSites: Map<String, SiteInfo>? = null

    /**
     * Returns the full site catalog, downloading it on the first call and serving
     * subsequent calls from the in-memory cache.
     *
     * @param forceRefresh If true, bypasses the cache and re-downloads the catalog.
     * @return A map from site name (e.g. "GitHub") to its [SiteInfo] configuration.
     */
    override suspend fun loadSites(forceRefresh: Boolean): Map<String, SiteInfo> {
        cachedSites?.takeUnless { forceRefresh }?.let { return it }

        return withContext(Dispatchers.Default) {
            val rawJson = httpClient.get(catalogUrl).bodyAsText()
            val parsed = parseCatalog(rawJson)
            cachedSites = parsed
            parsed
        }
    }

    /**
     * Parses the raw catalog JSON into a Map<String, SiteInfo>.
     *
     * The catalog is a flat object where each top-level key is a site name and each value
     * is a SiteInfo object. We also drop the "${'$'}schema" entry which is JSON schema metadata,
     * not an actual site.
     */
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