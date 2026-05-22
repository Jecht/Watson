package com.kapps.watson.core.repository

import com.kapps.watson.core.model.SiteInfo

interface SitesRepository {
    suspend fun loadSites(forceRefresh: Boolean = false): Map<String, SiteInfo>
}