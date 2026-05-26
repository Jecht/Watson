package com.kapps.watson.domain.usecase

import com.kapps.watson.core.domain.usecase.ScanUsernameUseCase
import com.kapps.watson.core.model.QueryStatus
import com.kapps.watson.di.sharedModules
import com.kapps.watson.runIntegrationTest
import kotlinx.coroutines.flow.toList
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class ScanUsernameUseCaseTest : KoinTest {

    private val scanUsernameUseCase: ScanUsernameUseCase by inject()

    @BeforeTest
    fun setUp() {
        startKoin { modules(sharedModules) }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun scan_existingUser_yieldsManyClaimedAccounts() = runIntegrationTest(timeout = 3.minutes) {
        val username = "torvalds"

        val results = scanUsernameUseCase(username = username).toList()

        val claimed = results.filter { result -> result.status == QueryStatus.CLAIMED }
        val available = results.filter { result -> result.status == QueryStatus.AVAILABLE }
        val unknown = results.filter { result -> result.status == QueryStatus.UNKNOWN }
        val waf = results.filter { result -> result.status == QueryStatus.WAF }
        val illegal = results.filter { result -> result.status == QueryStatus.ILLEGAL }

        println("=== Scan results for '$username' ===")
        println("Total probed sites : ${results.size}")
        println("Claimed            : ${claimed.size}")
        println("Available          : ${available.size}")
        println("Unknown (errors)   : ${unknown.size}")
        println("WAF blocked        : ${waf.size}")
        println("Illegal            : ${illegal.size}")
        println()
        println("=== CLAIMED sites ===")
        claimed.forEach { result -> println("  ✓ ${result.siteName}: ${result.siteUrl}") }

        assertTrue(results.isNotEmpty(), "Scan should yield results")
        assertTrue(claimed.isNotEmpty(), "torvalds should be claimed on at least one site")
    }
}