package com.kapps.watson.core.domain

import com.kapps.watson.core.model.HttpMethod
import com.kapps.watson.core.model.ProbeResponse
import com.kapps.watson.core.domain.network.SiteProbeService

/**
 * Test double for [SiteProbeService] that returns a scripted response or throws on demand.
 *
 * Each test instantiates one of these with the exact response it wants the production code
 * to see, then drives ProbeSiteUseCase to verify the resulting QueryResult.
 */
internal class FakeSiteProbeService private constructor(
    private val response: ProbeResponse?,
    private val error: Throwable?,
) : SiteProbeService {

    override suspend fun probe(
        url: String,
        method: HttpMethod,
        headers: Map<String, String>?,
    ): ProbeResponse {
        error?.let { throw it }
        return response!!
    }

    companion object {
        /** Builds a fake that responds with [response]. */
        fun returning(response: ProbeResponse): FakeSiteProbeService =
            FakeSiteProbeService(response = response, error = null)

        /** Builds a fake that always throws [error] when probed. */
        fun throwing(error: Throwable): FakeSiteProbeService =
            FakeSiteProbeService(response = null, error = error)
    }
}