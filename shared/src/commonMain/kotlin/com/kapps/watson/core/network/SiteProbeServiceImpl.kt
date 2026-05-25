package com.kapps.watson.core.network

import com.kapps.watson.core.domain.network.SiteProbeService
import com.kapps.watson.core.model.HttpMethod
import com.kapps.watson.core.model.ProbeResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request

/**
 * Ktor-backed implementation of [com.kapps.watson.core.domain.network.SiteProbeService].
 *
 * This is the only place in the application where Ktor types appear when probing a site.
 * It translates between the domain's transport-agnostic primitives and Ktor's API,
 * isolating the rest of the codebase from any HTTP library specifics.
 */
internal class SiteProbeServiceImpl(
    private val httpClient: HttpClient,
) : SiteProbeService {

    override suspend fun probe(
        url: String,
        method: HttpMethod,
        headers: Map<String, String>?,
    ): ProbeResponse {
        val response = httpClient.request(url) {
            this.method = method.toKtor()
            if (!headers.isNullOrEmpty()) {
                headers {
                    headers.forEach { (name, value) -> append(name, value) }
                }
            }
        }

        return ProbeResponse(
            statusCode = response.status.value,
            body = runCatching { response.bodyAsText() }.getOrDefault(""),
            finalUrl = response.request.url.toString(),
        )
    }

    /**
     * Maps the domain's HttpMethod enum to Ktor's equivalent.
     * This translation is the entire purpose of this class.
     */
    private fun HttpMethod.toKtor(): io.ktor.http.HttpMethod = when (this) {
        HttpMethod.GET -> io.ktor.http.HttpMethod.Get
        HttpMethod.HEAD -> io.ktor.http.HttpMethod.Head
        HttpMethod.POST -> io.ktor.http.HttpMethod.Post
        HttpMethod.PUT -> io.ktor.http.HttpMethod.Put
    }
}