package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.domain.network.SiteProbeService
import com.kapps.watson.core.model.*
import kotlin.time.TimeSource

internal class ProbeSiteUseCaseImpl(
    private val siteProbeService: SiteProbeService,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val buildProbeUrlUseCase: BuildProbeUrlUseCase,
    private val detectWafUseCase: DetectWafUseCase,
) : ProbeSiteUseCase {

    override suspend fun invoke(
        username: String,
        siteName: String,
        site: SiteInfo,
    ): QueryResult {
        val profileUrl = buildProbeUrlUseCase(urlPattern = site.urlPattern, username = username)

        // Short-circuit when the username can't possibly exist on this site.
        if (!validateUsernameUseCase(username = username, site = site)) {
            return QueryResult(
                username = username,
                siteName = siteName,
                siteUrl = profileUrl,
                status = QueryStatus.ILLEGAL,
            )
        }

        // The probing URL may differ from the profile URL (urlProbe override).
        val probeUrl = site.urlProbe
            ?.let { pattern -> buildProbeUrlUseCase(urlPattern = pattern, username = username) }
            ?: profileUrl

        return runCatching {
            val startMark = TimeSource.Monotonic.markNow()
            val response = siteProbeService.probe(
                url = probeUrl,
                method = resolveHttpMethod(site = site),
                headers = site.headers,
            )
            val elapsedTime = startMark.elapsedNow()
            val status = interpretResponse(response = response, site = site)

            QueryResult(
                username = username,
                siteName = siteName,
                siteUrl = profileUrl,
                status = status,
                queryTime = elapsedTime,
            )
        }.getOrElse { error ->
            QueryResult(
                username = username,
                siteName = siteName,
                siteUrl = profileUrl,
                status = QueryStatus.UNKNOWN,
                errorContext = error.message ?: error::class.simpleName,
            )
        }
    }

    /**
     * Resolves which HTTP method to use, mirroring the logic in sherlock.py:
     * - explicit request_method if declared
     * - HEAD by default for status_code detection (saves bandwidth)
     * - GET otherwise (we need the body)
     */
    private fun resolveHttpMethod(site: SiteInfo): HttpMethod =
        HttpMethod.parse(site.requestMethod)
            ?: if (site.errorType == "status_code") HttpMethod.HEAD else HttpMethod.GET

    /**
     * Interprets the probe response according to the site's declared detection strategy.
     *
     * Order of checks matters: WAF detection runs first because a WAF page would otherwise
     * be misclassified as CLAIMED by every other strategy.
     */
    private fun interpretResponse(response: ProbeResponse, site: SiteInfo): QueryStatus {
        if (detectWafUseCase(responseBody = response.body)) {
            return QueryStatus.WAF
        }

        return when (site.errorType) {
            "status_code" -> interpretByStatusCode(response = response, site = site)
            "message" -> interpretByMessage(response = response, site = site)
            "response_url" -> interpretByResponseUrl(response = response)
            else -> QueryStatus.UNKNOWN
        }
    }

    /**
     * Strategy: AVAILABLE when the response code matches one declared in errorCode,
     * or when the status falls outside the 2xx success range. CLAIMED otherwise.
     */
    private fun interpretByStatusCode(response: ProbeResponse, site: SiteInfo): QueryStatus {
        val expectedErrorCodes = site.errorCodes()
        val statusCode = response.statusCode

        return when {
            expectedErrorCodes.isNotEmpty() && statusCode in expectedErrorCodes -> QueryStatus.AVAILABLE
            statusCode !in 200..299 -> QueryStatus.AVAILABLE
            else -> QueryStatus.CLAIMED
        }
    }

    /**
     * Strategy: AVAILABLE when any of the configured error messages appears in
     * the response body. CLAIMED otherwise.
     */
    private fun interpretByMessage(response: ProbeResponse, site: SiteInfo): QueryStatus {
        val errorMessages = site.errorMessages()
        val errorPresent = errorMessages.any { message -> message in response.body }
        return if (errorPresent) QueryStatus.AVAILABLE else QueryStatus.CLAIMED
    }

    /**
     * Strategy: CLAIMED when the response is a 2xx. The site is expected to redirect
     * to an error page or return 4xx/5xx when the username doesn't exist.
     */
    private fun interpretByResponseUrl(response: ProbeResponse): QueryStatus {
        val statusCode = response.statusCode
        return if (statusCode in 200..299) QueryStatus.CLAIMED else QueryStatus.AVAILABLE
    }
}