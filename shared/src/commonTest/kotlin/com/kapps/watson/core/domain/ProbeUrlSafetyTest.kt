package com.kapps.watson.core.domain

import com.kapps.watson.core.domain.usecase.isProbeUrlSafe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [isProbeUrlSafe], the guard that refuses to probe unsafe catalog-supplied URLs.
 */
class ProbeUrlSafetyTest {

    @Test
    fun allows_plainHttpsDomain() {
        assertTrue(isProbeUrlSafe("https://github.com/torvalds"))
        assertTrue(isProbeUrlSafe("https://sub.example.co.uk/users/john?tab=repos"))
    }

    @Test
    fun rejects_nonHttpsSchemes() {
        assertFalse(isProbeUrlSafe("http://example.com/john"))
        assertFalse(isProbeUrlSafe("file:///etc/passwd"))
        assertFalse(isProbeUrlSafe("ftp://example.com/john"))
    }

    @Test
    fun rejects_ipLiteralAndMetadataHosts() {
        assertFalse(isProbeUrlSafe("https://169.254.169.254/latest/meta-data/"))
        assertFalse(isProbeUrlSafe("https://127.0.0.1/john"))
        assertFalse(isProbeUrlSafe("https://10.0.0.5/john"))
        assertFalse(isProbeUrlSafe("https://[::1]/john"))
    }

    @Test
    fun rejects_localhostAndGarbage() {
        assertFalse(isProbeUrlSafe("https://localhost/john"))
        assertFalse(isProbeUrlSafe("https://app.localhost/john"))
        assertFalse(isProbeUrlSafe("not a url at all"))
        assertEquals(false, isProbeUrlSafe(""))
    }
}
