package com.kapps.watson

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Helper for integration tests that perform real I/O.
 *
 * Uses [runTest] for scope management but switches to [Dispatchers.Default] so that
 * actual network calls are executed instead of being virtualized away by the test scheduler.
 */
fun runIntegrationTest(
    timeout: Duration = 30.seconds,
    block: suspend CoroutineScope.() -> Unit,
) = runTest(timeout = timeout) {
    withContext(Dispatchers.Default) {
        block()
    }
}