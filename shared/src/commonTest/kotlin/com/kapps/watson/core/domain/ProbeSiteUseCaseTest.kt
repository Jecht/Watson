package com.kapps.watson.core.domain

import com.kapps.watson.core.domain.usecase.ProbeSiteUseCase
import com.kapps.watson.core.model.QueryStatus
import com.kapps.watson.core.repository.SitesRepository
import com.kapps.watson.di.sharedModules
import com.kapps.watson.runIntegrationTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProbeSiteUseCaseTest : KoinTest {

    private val probeSiteUseCase: ProbeSiteUseCase by inject()
    private val sitesRepository: SitesRepository by inject()

    @BeforeTest
    fun setUp() {
        startKoin { modules(sharedModules) }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun probe_existingGitHubUser_returnsClaimed() = runIntegrationTest {
        val sites = sitesRepository.loadSites()
        val gitHub = sites.getValue("GitHub")

        val result = probeSiteUseCase(
            username = "torvalds",
            siteName = "GitHub",
            site = gitHub,
        )

        println("Result: $result")
        assertEquals(QueryStatus.CLAIMED, result.status)
    }

    @Test
    fun probe_unlikelyGitHubUser_returnsAvailable() = runIntegrationTest {
        val sites = sitesRepository.loadSites()
        val gitHub = sites.getValue("GitHub")

        // Username valid for GitHub (alphanum + dashes, ≤ 39 chars) but very unlikely to exist.
        val result = probeSiteUseCase(
            username = "watson-test-zzz-9k3m7q",
            siteName = "GitHub",
            site = gitHub,
        )

        println("Result: $result")
        assertEquals(QueryStatus.AVAILABLE, result.status)
    }
}