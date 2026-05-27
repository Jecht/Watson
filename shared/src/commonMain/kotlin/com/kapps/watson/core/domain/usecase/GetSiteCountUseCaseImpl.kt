package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.repository.SitesRepository

internal class GetSiteCountUseCaseImpl(
    private val sitesRepository: SitesRepository,
) : GetSiteCountUseCase {

    override suspend fun invoke(): Int {
        val sites = sitesRepository.loadSites()
        return sites.size
    }
}