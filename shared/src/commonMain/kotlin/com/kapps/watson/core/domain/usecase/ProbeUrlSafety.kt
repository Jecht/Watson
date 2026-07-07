package com.kapps.watson.core.domain.usecase

import io.ktor.http.URLProtocol
import io.ktor.http.Url

/**
 * Decides whether it is safe to send a network probe to [rawUrl].
 *
 * The `url`/`urlProbe` patterns come from the remotely-fetched Sherlock catalog, so a compromised
 * entry could point a probe at an internal or cloud-metadata endpoint (SSRF) or at a cleartext
 * origin. We therefore only ever probe HTTPS URLs whose host is a real domain name, rejecting:
 *  - any non-HTTPS scheme (`http`, `file`, `ftp`, ...); cleartext is already blocked on Android and
 *    iOS, so enforcing it here just makes the desktop behave consistently and closes the SSRF path,
 *  - IP-literal hosts (v4 and v6), which is how loopback / link-local / metadata targets are named
 *    (`127.0.0.1`, `169.254.169.254`, `::1`, ...),
 *  - `localhost` and empty hosts.
 *
 * This blocks the direct, realistic SSRF vectors. It does not resolve DNS, so a hostile catalog
 * could still hide an internal address behind a public domain name; defending against that would
 * require host resolution we cannot safely do on-device.
 */
internal fun isProbeUrlSafe(rawUrl: String): Boolean {
    val url = runCatching { Url(rawUrl) }.getOrNull() ?: return false
    if (url.protocol != URLProtocol.HTTPS) return false

    val host = url.host.lowercase()
    if (host.isEmpty()) return false
    if (host == "localhost" || host.endsWith(".localhost")) return false
    if (host.isIpLiteral()) return false

    return true
}

/** True when [this] host is a raw IPv4 or IPv6 literal rather than a domain name. */
private fun String.isIpLiteral(): Boolean {
    if (contains(':')) return true // IPv6 literal (Ktor strips the surrounding brackets from the host)
    val octets = split('.')
    return octets.size == 4 && octets.all { octet -> octet.toIntOrNull() in 0..255 }
}
