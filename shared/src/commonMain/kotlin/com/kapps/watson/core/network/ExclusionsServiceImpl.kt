package com.kapps.watson.core.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ExclusionsServiceImpl(
    private val httpClient: HttpClient,
) : ExclusionsService {

    override suspend fun loadExclusions(): Set<String> = withContext(Dispatchers.Default) {
        val exclusionsUrl =
            "https://raw.githubusercontent.com/sherlock-project/sherlock/refs/heads/exclusions/false_positive_exclusions.txt"
        runCatching {
            val rawText = httpClient.get(exclusionsUrl).bodyAsText()
            rawText
                .lineSequence()
                .map { line -> line.trim() }
                .filter { line -> line.isNotEmpty() && line.startsWith("#").not() }
                .toSet()
        }.getOrDefault(emptySet())
    }
}