package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.QueryResult
import com.kapps.watson.core.model.SiteInfo

/**
 * Probes a single site to determine whether a given username exists on it.
 *
 * This is the core domain rule of Sherlock: take a username and a site configuration,
 * send the appropriate request, and interpret the response according to the site's
 * declared detection strategy (status_code, message, or response_url).
 *
 * The transport is delegated to [SiteProbeService], so this use case has no awareness
 * of the underlying HTTP library.
 */
interface ProbeSiteUseCase {

    /**
     * Probes [site] for [username] and returns a [QueryResult] describing the outcome.
     *
     * This function never throws: any transport error is captured in the returned result
     * with status [QueryStatus.UNKNOWN] and an explanatory message in [QueryResult.errorContext].
     */
    suspend operator fun invoke(
        username: String,
        siteName: String,
        site: SiteInfo,
    ): QueryResult
}