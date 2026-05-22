package com.kapps.watson.core.repository

import com.kapps.watson.di.appModules
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SitesRepositoryTest : KoinTest {

    private val repository: SitesRepository by inject()

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(appModules)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadSites_downloadsAndParsesCatalog() = runTest(timeout = 30.seconds) {
        val sites = repository.loadSites()

        println("Catalog loaded with ${sites.size} sites")
        println("First 5 sites: ${sites.keys.take(5)}")
        println("GitHub config: ${sites["GitHub"]}")

        assertTrue(sites.isNotEmpty(), "Catalog should not be empty")
        assertTrue(sites.size > 100, "Catalog should contain hundreds of sites")
        assertTrue(sites.containsKey("GitHub"), "Catalog should contain GitHub")
    }
}