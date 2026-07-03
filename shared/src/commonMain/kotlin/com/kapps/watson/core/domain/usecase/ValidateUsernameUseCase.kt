package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.SiteInfo

/**
 * Validates that a username is acceptable for a given site according to its regex constraint.
 *
 * Many sites enforce username rules (length, allowed characters, ...). When a site declares
 * a `regexCheck`, we can short-circuit the HTTP request entirely if the username does not
 * match: it would never exist on that site by construction.
 *
 * Port of the `regex_check` short-circuit from sherlock.py.
 */
interface ValidateUsernameUseCase {

    /**
     * Returns true if [username] is acceptable for [site], either because the site has
     * no regex constraint or because the username matches it.
     *
     * Suspending because the regex — which originates from the remotely-fetched Sherlock
     * catalog — is evaluated under a timeout to contain catastrophic-backtracking (ReDoS)
     * patterns. On timeout we fall back to permissive behaviour (probe the site).
     */
    suspend operator fun invoke(username: String, site: SiteInfo): Boolean
}