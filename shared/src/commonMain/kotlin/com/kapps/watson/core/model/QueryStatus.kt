package com.kapps.watson.core.model

/**
 * Status of a username lookup on a given site.
 * Direct port of the QueryStatus enum from result.py in Sherlock.
 */
enum class QueryStatus {
    /** Username found on the site (account exists). */
    CLAIMED,

    /** Username not found on the site (account is available). */
    AVAILABLE,

    /** Error during the request (network, timeout, parsing failure, ...). */
    UNKNOWN,

    /** Username is invalid for this site (does not match the required format or regex). */
    ILLEGAL,

    /** Request blocked by a Web Application Firewall (Cloudflare, AWS WAF, ...). */
    WAF,
}