package com.kapps.watson.core.model

import kotlin.time.Duration

/**
 * Result of a username lookup on a given site.
 * Direct port of the QueryResult class from result.py in Sherlock.
 *
 * @property username The username that was searched.
 * @property siteName Display name of the site that was tested (e.g. "GitHub").
 * @property siteUrl Full URL of the user profile on this site (e.g. "https://github.com/torvalds").
 *                   Remains valid even when the account does not exist — this is the URL the account
 *                   would have, were it to exist.
 * @property status Status of the query (CLAIMED, AVAILABLE, ...).
 * @property queryTime HTTP request duration. Null when the query failed before
 *                       sending (illegal username, configuration error, ...).
 * @property errorContext Technical detail in case of an error (e.g. "Connection timeout"). Null otherwise.
 */
data class QueryResult(
    val username: String,
    val siteName: String,
    val siteUrl: String,
    val status: QueryStatus,
    val queryTime: Duration? = null,
    val errorContext: String? = null,
)