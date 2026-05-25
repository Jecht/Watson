package com.kapps.watson.core.domain

import com.kapps.watson.core.domain.network.SiteProbeService
import com.kapps.watson.core.model.HttpMethod
import com.kapps.watson.core.model.ProbeResponse

class FakeSiteProbeService(
    private val response: ProbeResponse,
) : SiteProbeService {
    override suspend fun probe(url: String, method: HttpMethod, headers: Map<String, String>?) = response
}