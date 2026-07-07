package com.kapps.watson.core.domain

import com.kapps.watson.core.domain.usecase.BuildProbeUrlUseCaseImpl
import com.kapps.watson.core.domain.usecase.DetectWafUseCaseImpl
import com.kapps.watson.core.domain.usecase.ProbeSiteUseCase
import com.kapps.watson.core.domain.usecase.ProbeSiteUseCaseImpl
import com.kapps.watson.core.domain.usecase.ValidateUsernameUseCaseImpl
import com.kapps.watson.core.model.ProbeResponse
import com.kapps.watson.core.model.QueryStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Pure unit tests for [com.kapps.watson.core.domain.usecase.ProbeSiteUseCase].
 *
 * These tests run without any real network call — they wire the production
 * implementation against a [FakeSiteProbeService] that returns scripted responses.
 * They cover every detection strategy (status_code, message, response_url),
 * WAF detection, ILLEGAL short-circuit, and UNKNOWN fallback on transport errors.
 */
class ProbeSiteUseCaseUnitTest {

    /** Builds a use case wired against the supplied [siteProbeService]. */
    private fun buildUseCase(siteProbeService: FakeSiteProbeService): ProbeSiteUseCase =
        ProbeSiteUseCaseImpl(
            siteProbeService = siteProbeService,
            validateUsernameUseCase = ValidateUsernameUseCaseImpl(),
            buildProbeUrlUseCase = BuildProbeUrlUseCaseImpl(),
            detectWafUseCase = DetectWafUseCaseImpl(),
        )

    // ─── ILLEGAL short-circuit ───

    @Test
    fun probe_withUsernameViolatingRegex_returnsIllegal() = runTest {
        val site = testSite(regexCheck = "^[a-z]{3,10}$")
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 200, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "TOO_LONG_AND_UPPER", siteName = "Example", site = site)

        assertEquals(QueryStatus.ILLEGAL, result.status)
        assertEquals(null, result.queryTime) // never sent
    }

    @Test
    fun probe_withUnsafeProbeUrl_returnsIllegalWithoutProbing() = runTest {
        // A compromised catalog entry pointing at the cloud metadata endpoint must never be probed.
        val site = testSite(urlPattern = "https://169.254.169.254/latest/meta-data/{}")
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 200, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.ILLEGAL, result.status)
        assertEquals(null, result.queryTime) // never sent
    }

    @Test
    fun probe_withCleartextProbeUrl_returnsIllegalWithoutProbing() = runTest {
        val site = testSite(
            urlPattern = "https://example.com/{}",
            urlProbe = "http://example.com/api/{}", // urlProbe overrides the target and is cleartext
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 200, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.ILLEGAL, result.status)
        assertEquals(null, result.queryTime)
    }

    // ─── Strategy: status_code ───

    @Test
    fun probe_withStatusCodeStrategyAndErrorCodeMatch_returnsAvailable() = runTest {
        val site = testSite(
            errorType = "status_code",
            errorCode = errorCodeOf(404),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 404, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    @Test
    fun probe_withStatusCodeStrategyAndSuccessfulResponse_returnsClaimed() = runTest {
        val site = testSite(
            errorType = "status_code",
            errorCode = errorCodeOf(404),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 200, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.CLAIMED, result.status)
    }

    @Test
    fun probe_withStatusCodeStrategyAndServerError_returnsAvailable() = runTest {
        val site = testSite(
            errorType = "status_code",
            errorCode = errorCodeOf(404),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 500, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    @Test
    fun probe_withStatusCodeStrategyAndMultipleErrorCodes_recognizesAll() = runTest {
        val site = testSite(
            errorType = "status_code",
            errorCode = errorCodesOf(404, 410),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(statusCode = 410, body = "", finalUrl = ""),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    // ─── Strategy: message ───

    @Test
    fun probe_withMessageStrategyAndErrorMessageInBody_returnsAvailable() = runTest {
        val site = testSite(
            errorType = "message",
            errorMessage = errorMessageOf("User not found"),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 200,
                    body = "<html><body>User not found</body></html>",
                    finalUrl = "",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    @Test
    fun probe_withMessageStrategyAndNoErrorMessageInBody_returnsClaimed() = runTest {
        val site = testSite(
            errorType = "message",
            errorMessage = errorMessageOf("User not found"),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 200,
                    body = "<html><body>Welcome anyone!</body></html>",
                    finalUrl = "",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.CLAIMED, result.status)
    }

    @Test
    fun probe_withMessageStrategyAndAnyOfMultipleMessagesPresent_returnsAvailable() = runTest {
        val site = testSite(
            errorType = "message",
            errorMessage = errorMessagesOf("Not found", "Page does not exist"),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 200,
                    body = "Sorry, this Page does not exist on our service.",
                    finalUrl = "",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    // ─── Strategy: response_url ───

    @Test
    fun probe_withResponseUrlStrategyAndSuccess_returnsClaimed() = runTest {
        val site = testSite(errorType = "response_url")
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 200,
                    body = "",
                    finalUrl = "https://example.com/anyone",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.CLAIMED, result.status)
    }

    @Test
    fun probe_withResponseUrlStrategyAndNotFound_returnsAvailable() = runTest {
        val site = testSite(errorType = "response_url")
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 404,
                    body = "",
                    finalUrl = "https://example.com/404",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.AVAILABLE, result.status)
    }

    // ─── WAF detection ───

    @Test
    fun probe_withCloudflareChallengePageInBody_returnsWaf() = runTest {
        val cloudflareSnippet =
            "<span id=\"challenge-error-text\">Sorry, you have been blocked</span>"
        val site = testSite(
            errorType = "status_code",
            errorCode = errorCodeOf(404),
        )
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.returning(
                response = ProbeResponse(
                    statusCode = 200,
                    body = cloudflareSnippet,
                    finalUrl = "",
                ),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.WAF, result.status)
    }

    // ─── UNKNOWN fallback on transport error ───

    @Test
    fun probe_whenProbeServiceThrows_returnsUnknownWithErrorContext() = runTest {
        val site = testSite()
        val useCase = buildUseCase(
            siteProbeService = FakeSiteProbeService.throwing(
                error = RuntimeException("Connection reset by peer"),
            ),
        )

        val result = useCase(username = "anyone", siteName = "Example", site = site)

        assertEquals(QueryStatus.UNKNOWN, result.status)
        assertNotNull(result.errorContext)
    }
}