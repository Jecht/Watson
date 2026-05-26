package com.kapps.watson.core.domain

import com.kapps.watson.core.model.SiteInfo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

/**
 * Builds a minimal [SiteInfo] for tests. Only the fields relevant to the scenario
 * need to be passed explicitly; everything else has sensible defaults.
 */
internal fun testSite(
    urlPattern: String = "https://example.com/{}",
    urlMain: String = "https://example.com/",
    usernameClaimed: String = "claimed",
    errorType: String = "status_code",
    errorMessage: JsonElement? = null,
    errorCode: JsonElement? = null,
    regexCheck: String? = null,
    urlProbe: String? = null,
    requestMethod: String? = null,
    isNSFW: Boolean = false,
    headers: Map<String, String>? = null,
): SiteInfo = SiteInfo(
    urlPattern = urlPattern,
    urlMain = urlMain,
    usernameClaimed = usernameClaimed,
    errorType = errorType,
    errorMessage = errorMessage,
    errorCode = errorCode,
    regexCheck = regexCheck,
    urlProbe = urlProbe,
    requestMethod = requestMethod,
    isNSFW = isNSFW,
    headers = headers,
)

/** Convenience wrapper for declaring an error code as a single Int. */
internal fun errorCodeOf(code: Int): JsonElement = JsonPrimitive(code)

/** Convenience wrapper for declaring multiple error codes. */
internal fun errorCodesOf(vararg codes: Int): JsonElement = buildJsonArray {
    codes.forEach { code -> add(JsonPrimitive(code)) }
}

/** Convenience wrapper for declaring a single error message. */
internal fun errorMessageOf(message: String): JsonElement = JsonPrimitive(message)

/** Convenience wrapper for declaring multiple error messages. */
internal fun errorMessagesOf(vararg messages: String): JsonElement = buildJsonArray {
    messages.forEach { message -> add(JsonPrimitive(message)) }
}