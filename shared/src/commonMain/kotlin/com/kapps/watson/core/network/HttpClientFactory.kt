package com.kapps.watson.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/** Shared Json parser configured to be lenient with the variability of real-world JSON. */
fun createJson(): Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

/**
 * Builds a new HttpClient ready for use across the application.
 * The platform-specific engine (OkHttp on Android, Darwin on iOS, CIO on JVM)
 * is auto-selected by Ktor based on which engine module is on the classpath.
 */
fun createHttpClient(json: Json): HttpClient = HttpClient {
    expectSuccess = false
    followRedirects = true

    // Default timeout for any HTTP request, in milliseconds.
    val defaultTimeout = 60.seconds

    install(HttpTimeout) {
        requestTimeoutMillis = defaultTimeout.inWholeMilliseconds
        connectTimeoutMillis = defaultTimeout.inWholeMilliseconds
        socketTimeoutMillis = defaultTimeout.inWholeMilliseconds
    }

    install(UserAgent) {
        /**
         * User-Agent string that mimics a recent Firefox browser.
         * Many sites block requests with default Ktor/OkHttp User-Agents,
         * which would massively inflate the WAF false-positive rate.
         */
        agent = "Mozilla/5.0 (X11; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0"
    }

    // Add default browser-like headers on every request.
    // Some sites (notably GitHub) will silently hang on requests that omit these.
    install(DefaultRequest) {
        header(HttpHeaders.Accept, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.5")
        header(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
    }

    // Transparently negotiate and decompress gzip/deflate responses.
    // Without this, gzipped bodies would arrive as raw binary and break text parsing.
    install(ContentEncoding) {
        mode = ContentEncodingConfig.Mode.DecompressResponse
        gzip()
        deflate()
    }

    install(ContentNegotiation) {
        json(json)
    }

    install(Logging) {
        level = LogLevel.NONE
        logger = object : Logger {
            override fun log(message: String) {
                println("[Ktor] $message")
            }
        }
    }
}