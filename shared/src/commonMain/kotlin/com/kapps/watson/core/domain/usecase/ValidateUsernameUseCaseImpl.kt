package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.SiteInfo

internal class ValidateUsernameUseCaseImpl : ValidateUsernameUseCase {

    /**
     * Matches `{,N}` quantifiers where the lower bound is implicit (Python syntax).
     * Java regex requires an explicit `{0,N}` form, so we rewrite the pattern before compiling.
     */
    private val implicitLowerBoundQuantifier = Regex("""\{,(\d+)}""")

    override fun invoke(username: String, site: SiteInfo): Boolean {
        val rawPattern = site.regexCheck ?: return true
        val normalizedPattern = normalizeForJavaRegex(rawPattern)

        return runCatching {
            Regex(normalizedPattern).containsMatchIn(username)
        }.getOrElse {
            // If the regex is invalid even after normalization, fall back to permissive behaviour:
            // we'd rather probe the site and get AVAILABLE/CLAIMED than skip it with ILLEGAL.
            true
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