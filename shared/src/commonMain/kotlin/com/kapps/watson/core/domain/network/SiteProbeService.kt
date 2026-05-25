package com.kapps.watson.core.domain.network

import com.kapps.watson.core.model.HttpMethod
import com.kapps.watson.core.model.ProbeResponse

/**
 * Abstract transport for sending a single HTTP probe to a site.
 *
 * This interface lives in the domain layer and intentionally exposes only domain-level
 * primitives ([HttpMethod], [ProbeResponse]) — never the underlying HTTP library.
 * The implementation is provided by the infrastructure layer.
 */
interface SiteProbeService {

    /**
     * Sends a single probe request and returns its [ProbeResponse].
     *
     * Errors are surfaced as exceptions; callers (typically use cases) are expected to
     * wrap calls in `runCatching` or equivalent.
     */
    suspend fun probe(
        url: String,
        method: HttpMethod,
        headers: Map<String, String>?,
    ): ProbeResponse
}