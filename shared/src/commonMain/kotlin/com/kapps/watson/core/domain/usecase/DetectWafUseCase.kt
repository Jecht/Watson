package com.kapps.watson.core.domain.usecase

/**
 * Detects whether an HTTP response body comes from a Web Application Firewall
 * (Cloudflare, AWS WAF, PerimeterX, ...) rather than the target site itself.
 *
 * When a WAF intercepts a request, it typically returns a 200 OK with a challenge page,
 * which would otherwise be misinterpreted as a CLAIMED username. Detecting these pages
 * is essential to avoid false positives.
 *
 * Each fingerprint is a snippet of HTML or JavaScript that is highly specific to a
 * given WAF's challenge page. The dates in the comments indicate when each fingerprint
 * was last verified. WAFs evolve, so this list needs periodic updates.
 *
 * Port of the WAFHitMsgs list from sherlock.py.
 */
interface DetectWafUseCase {

    /**
     * Returns true if [responseBody] contains any known WAF challenge fingerprint.
     */
    operator fun invoke(responseBody: String): Boolean
}