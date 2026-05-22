package com.kapps.watson.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents the configuration of a single site from Sherlock's data.json.
 * The site name is the JSON dictionary key, so it is not included in this class
 * (we will recover it during parsing using a Map<String, SiteInfo>).
 *
 * Reference format: https://raw.githubusercontent.com/sherlock-project/sherlock/master/sherlock_project/resources/data.json
 */
@Serializable
data class SiteInfo(
    /** URL template with {} as the placeholder for the username. Example: "https://github.com/{}". */
    @SerialName("url")
    val urlPattern: String,

    /** Home URL of the site. Example: "https://github.com/". */
    @SerialName("urlMain")
    val urlMain: String,

    /** A username known to exist on this site (used by Sherlock for testing purposes). */
    @SerialName("username_claimed")
    val usernameClaimed: String,

    /** Detection strategy: "status_code", "message", or "response_url". */
    @SerialName("errorType")
    val errorType: String,

    /**
     * Error message(s) to search for in the HTML response to detect a missing account.
     * Can be either a single String or a List<String> in the source JSON.
     * Use errorMessages() to get a uniform List<String>.
     */
    @SerialName("errorMsg")
    val errorMessage: JsonElement? = null,

    /**
     * HTTP status code(s) indicating that the account does not exist (e.g. 404).
     * Can be either a single Int or a List<Int> in the source JSON.
     * Use errorCodes() to get a uniform List<Int>.
     */
    @SerialName("errorCode")
    val errorCode: JsonElement? = null,

    /** Regex used to validate the username for this site. If defined and not matched → ILLEGAL. */
    @SerialName("regexCheck")
    val regexCheck: String? = null,

    /** Alternative URL used to probe for existence (instead of urlPattern). */
    @SerialName("urlProbe")
    val urlProbe: String? = null,

    /** HTTP method to use: "GET", "HEAD", "POST", "PUT". Defaults to GET (or HEAD for status_code). */
    @SerialName("request_method")
    val requestMethod: String? = null,

    /** True if the site contains NSFW content (filtered unless the user opts in). */
    @SerialName("isNSFW")
    val isNSFW: Boolean = false,

    /** Additional HTTP headers to send for this site. */
    @SerialName("headers")
    val headers: Map<String, String>? = null,
)

// ─── Extension helpers for polymorphic fields ───

/**
 * Returns errorMessage as a uniform List<String>, whether the source JSON had
 * a single String or a List<String>.
 */
fun SiteInfo.errorMessages(): List<String> = when (val message = errorMessage) {
    null -> emptyList()
    is JsonPrimitive -> listOfNotNull(message.contentOrNull)
    is JsonArray -> message.mapNotNull { it.jsonPrimitive.contentOrNull }
    else -> emptyList()
}

/**
 * Returns errorCode as a uniform List<Int>, whether the source JSON had
 * a single Int or a List<Int>.
 */
fun SiteInfo.errorCodes(): List<Int> = when (val code = errorCode) {
    null -> emptyList()
    is JsonPrimitive -> listOfNotNull(code.intOrNull)
    is JsonArray -> code.mapNotNull { it.jsonPrimitive.intOrNull }
    else -> emptyList()
}