package com.kapps.watson.core.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

/**
 * Upper bound, in bytes, on a single probed site's response body we will buffer in memory.
 *
 * Only the `message` detection strategy inspects the body, and its error strings live in the
 * first few KB of the HTML; 1 MiB is far more than enough while capping the blast radius of a
 * hostile site returning a multi-gigabyte or never-ending body.
 */
internal const val MAX_PROBE_BODY_BYTES: Long = 1L * 1024 * 1024

/** Upper bound, in bytes, on the remote catalog payload (data.json is a couple of MiB today). */
internal const val MAX_CATALOG_BYTES: Long = 25L * 1024 * 1024

/** Upper bound, in bytes, on the exclusions list (a short newline-delimited text file). */
internal const val MAX_EXCLUSIONS_BYTES: Long = 5L * 1024 * 1024

/**
 * Reads at most [maxBytes] of the response body as UTF-8 text, then aborts the rest of the download.
 *
 * Unlike [io.ktor.client.statement.bodyAsText], this never materializes an unbounded body in memory.
 * Because the [io.ktor.client.plugins.compression.ContentEncoding] plugin decodes the stream lazily,
 * capping the number of *decompressed* bytes we pull also defuses gzip/deflate decompression bombs:
 * a few KB of compressed input can no longer inflate into gigabytes of heap. All response bodies here
 * come from third parties (the catalog host and the ~400 probed sites), so this bound is load-bearing.
 */
internal suspend fun HttpResponse.bodyAsTextCapped(maxBytes: Long): String {
    val channel = bodyAsChannel()
    val accumulated = Buffer()
    var total = 0L

    while (total < maxBytes && !channel.isClosedForRead) {
        val packet = channel.readRemaining(maxBytes - total)
        val chunk = packet.readByteArray()
        if (chunk.isEmpty()) break
        accumulated.write(chunk)
        total += chunk.size
    }

    // Stop pulling (and decompressing) whatever body remains beyond the cap.
    channel.cancel(null)
    return accumulated.readByteArray().decodeToString()
}
