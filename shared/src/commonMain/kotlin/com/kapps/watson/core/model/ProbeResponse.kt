package com.kapps.watson.core.model

/**
 * Outcome of a site probe, decoupled from any specific HTTP library.
 *
 * Contains only the information needed by domain rules to determine whether a
 * username exists on a site: status code, body, and final URL after redirects.
 */
data class ProbeResponse(
    val statusCode: Int,
    val body: String,
    val finalUrl: String,
)