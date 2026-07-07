package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.SiteInfo
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

internal class ValidateUsernameUseCaseImpl : ValidateUsernameUseCase {

    /**
     * Dedicated thread pool for evaluating untrusted regexes, kept off [Dispatchers.Default].
     *
     * Catastrophic backtracking is non-suspending CPU work that ignores cooperative cancellation:
     * on timeout we can stop *waiting* for it, but the computation keeps burning its thread until
     * it finishes on its own (which, for a true ReDoS pattern, is effectively never). If those
     * threads came from [Dispatchers.Default] -- the pool the whole app runs on -- a handful of
     * hostile patterns would starve every other coroutine and freeze the UI. Confining the damage
     * to this small, separate pool means the worst case is stalled *validation*, not a dead app.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private val evaluationDispatcher = newFixedThreadPoolContext(
        // Size of the isolated regex pool -- small enough to contain stuck evaluations.
        nThreads = 2,
        name = "username-regex-eval",
    )
    private val evaluationScope = CoroutineScope(evaluationDispatcher + SupervisorJob())

    override suspend fun invoke(username: String, site: SiteInfo): Boolean {
        val rawPattern = site.regexCheck ?: return true

        // Longest input we ever hand to an untrusted regex. Bounds the cost of catastrophic
        // backtracking; comfortably above any real username length.
        val maxRegexInputLength = 100

        // Backtracking cost grows with input length, so bounding the evaluated input is the primary
        // ReDoS defence. Real usernames are far shorter than this cap; a longer string cannot be a
        // legitimate username, and the anchored validators used by Sherlock reject it anyway.
        val boundedInput = username.take(maxRegexInputLength)

        val normalizedPattern = normalizeForJavaRegex(rawPattern)
        val evaluation = evaluationScope.async {
            runCatching {
                Regex(normalizedPattern).containsMatchIn(boundedInput)
            }.getOrElse {
                // If the regex is invalid even after normalization, fall back to permissive
                // behaviour: we'd rather probe the site and get AVAILABLE/CLAIMED than skip it.
                true
            }
        }

        // Observe the timeout on Dispatchers.Default (never the evaluation pool, which may be fully
        // occupied by stuck evaluations) and pin it to wall-clock time so it behaves consistently
        // regardless of the caller's dispatcher (e.g. a virtual-time test clock).
        return withContext(Dispatchers.Default) {

            // 2 seconds is an upper bound on how long a single regex evaluation may run before we give up.
            // `regexCheck` patterns come from the remotely-fetched Sherlock catalog, so a
            // compromised or malformed entry could ship a pattern with catastrophic backtracking
            // (ReDoS). Bounding every evaluation stops one pathological site from stalling the scan.
            withTimeoutOrNull(timeout = 2.seconds) { evaluation.await() }
                ?: run {
                    evaluation.cancel()
                    true
                }
        }
    }

    /**
     * Normalizes a regex pattern originating from Sherlock's data.json (Python-flavored)
     * so that it is accepted by Java/Kotlin's regex engine.
     *
     * Known transformations:
     * - `{,N}` → `{0,N}` (implicit zero lower bound is unsupported in Java)
     */
    private fun normalizeForJavaRegex(pattern: String): String {

        // Matches `{,N}` quantifiers where the lower bound is implicit (Python syntax).
        // Java regex requires an explicit `{0,N}` form, so we rewrite the pattern before compiling.
        val implicitLowerBoundQuantifier = Regex("""\{,(\d+)\}""")

        return pattern.replace(implicitLowerBoundQuantifier) { match ->
            "{0,${match.groupValues[1]}}"
        }
    }
}
