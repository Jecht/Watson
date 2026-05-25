package com.kapps.watson.core.model

/**
 * HTTP method used to probe a site, decoupled from any specific HTTP library.
 *
 * Mirrors the subset of methods that Sherlock's data.json may declare in
 * the `request_method` field.
 */
enum class HttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    ;

    companion object {
        /**
         * Parses an HTTP method name (case-insensitive). Returns null when [value] is
         * either null or not a recognized method, in which case callers should fall back
         * to their default method.
         */
        fun parse(value: String?): HttpMethod? = value
            ?.uppercase()
            ?.let { name -> entries.firstOrNull { method -> method.name == name } }
    }
}