package com.kapps.watson.core.repository

import com.kapps.watson.core.model.SiteInfo

interface SitesRepository {
    suspend fun loadSites(forceRefresh: Boolean = false, honorExclusions: Boolean = true): Map<String, SiteInfo>
}