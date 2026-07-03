package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.SiteInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ValidateUsernameUseCaseImpl(
    /**
     * Upper bound on how long a single regex evaluation may run before we give up.
     *
     * `regexCheck` patterns come from the remotely-fetched Sherlock catalog, so a
     * compromised or malformed entry could ship a pattern with catastrophic backtracking
     * (ReDoS). Bounding every evaluation stops one pathological site from stalling the scan.
     */
    private val evaluationTimeout: Duration = 2.seconds,
) : ValidateUsernameUseCase {

    /**
     * Matches `{,N}` quantifiers where the lower bound is implicit (Python syntax).
     * Java regex requires an explicit `{0,N}` form, so we rewrite the pattern before compiling.
     */
    private val implicitLowerBoundQuantifier = Regex("""\{,(\d+)\}""")

    /**
     * Detached scope used to evaluate untrusted regexes off the caller's job.
     *
     * A pathological pattern ignores cooperative cancellation, so on timeout we abandon the
     * evaluation instead of joining it: the scan proceeds immediately while the stray
     * computation unwinds on its own, rather than blocking the whole scan indefinitely.
     */
    private val evaluationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun invoke(username: String, site: SiteInfo): Boolean {
        val rawPattern = site.regexCheck ?: return true
        val normalizedPattern = normalizeForJavaRegex(rawPattern)

        val evaluation = evaluationScope.async {
            runCatching {
                Regex(normalizedPattern).containsMatchIn(username)
            }.getOrElse {
                // If the regex is invalid even after normalization, fall back to permissive
                // behaviour: we'd rather probe the site and get AVAILABLE/CLAIMED than skip it.
                true
            }
        }

        // withContext(Dispatchers.Default) pins the timeout to wall-clock time so it behaves
        // consistently regardless of the caller's dispatcher (e.g. a virtual-time test clock).
        return withContext(Dispatchers.Default) {
            withTimeoutOrNull(evaluationTimeout) { evaluation.await() }
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
    private fun normalizeForJavaRegex(pattern: String): String =
        pattern.replace(implicitLowerBoundQuantifier) { match ->
            "{0,${match.groupValues[1]}}"
        }
}
