package com.kapps.watson.core.repository

import com.kapps.watson.core.model.SiteInfo
import com.kapps.watson.core.network.ExclusionsService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val exclusionsService: ExclusionsService,
) : SitesRepository {

    private val catalogUrl = "https://data.sherlockproject.xyz"

    private var cachedSites: Map<String, SiteInfo>? = null
    private var cachedExclusions: Set<String>? = null

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
        cachedSites?.takeUnless { forceRefresh }?.let { return it }

        return withContext(Dispatchers.Default) {
            coroutineScope {
                val rawCatalog = async { httpClient.get(catalogUrl).bodyAsText() }
                val exclusions = async { exclusionsService.loadExclusions() }

                val parsed = parseCatalog(rawCatalog.await())
                cachedSites = parsed
                cachedExclusions = exclusions.await()
                parsed
            }
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