package com.kapps.watson.core.domain

import com.kapps.watson.core.domain.usecase.ValidateUsernameUseCaseImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateUsernameUseCaseUnitTest {

    private val useCase = ValidateUsernameUseCaseImpl()

    @Test
    fun validate_withNoRegexCheck_returnsTrue() = runTest {
        val site = testSite(regexCheck = null)
        assertEquals(true, useCase(username = "anything", site = site))
    }

    @Test
    fun validate_withRegexCheckMatching_returnsTrue() = runTest {
        val site = testSite(regexCheck = "^[a-z]+$")
        assertEquals(true, useCase(username = "valid", site = site))
    }

    @Test
    fun validate_withRegexCheckNotMatching_returnsFalse() = runTest {
        val site = testSite(regexCheck = "^[a-z]+$")
        assertEquals(false, useCase(username = "Invalid", site = site))
    }

    @Test
    fun validate_withPythonStyleImplicitLowerBound_normalizesAndAccepts() = runTest {
        // Pattern that would crash without normalization (the bug we fixed earlier).
        val site = testSite(regexCheck = "^[a-z]{,10}$")
        assertEquals(true, useCase(username = "abc", site = site))
    }

    @Test
    fun validate_withMalformedRegex_doesNotCrashAndReturnsTrue() = runTest {
        // Defensive fallback: rather than blocking the scan, we let the probe happen.
        val site = testSite(regexCheck = "[unclosed bracket")
        assertEquals(true, useCase(username = "anything", site = site))
    }
}