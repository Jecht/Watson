package com.kapps.watson.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [bodyAsTextCapped], the bounded body reader that protects every network
 * call from a hostile third party returning an oversized (or never-ending) response.
 */
class HttpResponseCappedTest {

    private fun clientReturning(body: String): HttpClient =
        HttpClient(MockEngine { respond(content = body, status = HttpStatusCode.OK) })

    @Test
    fun bodyAsTextCapped_truncatesBodyLargerThanCap() = runTest {
        val cap = 1_024L
        // 64 KiB of ASCII -> 64 Ki bytes, far past the 1 KiB cap.
        val hugeBody = "a".repeat(64 * 1024)

        val text = clientReturning(hugeBody).get("https://example.com").bodyAsTextCapped(cap)

        assertTrue(text.length <= cap, "expected at most $cap chars, got ${text.length}")
    }

    @Test
    fun bodyAsTextCapped_returnsFullBodyWhenUnderCap() = runTest {
        val body = "<html><body>User not found</body></html>"

        val text = clientReturning(body).get("https://example.com").bodyAsTextCapped(MAX_PROBE_BODY_BYTES)

        assertEquals(body, text)
    }

    @Test
    fun bodyAsTextCapped_handlesEmptyBody() = runTest {
        val client = HttpClient(MockEngine {
            respond(content = "", status = HttpStatusCode.NoContent, headers = headersOf())
        })

        val text = client.get("https://example.com").bodyAsTextCapped(MAX_PROBE_BODY_BYTES)

        assertEquals("", text)
    }
}
