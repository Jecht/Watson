package com.kapps.watson.core.domain.usecase

/**
 * Builds a probing URL by injecting a URL-encoded username into a site's URL pattern.
 *
 * Sherlock URL patterns use {} as the placeholder for the username,
 * for example "https://github.com/{}" → "https://github.com/torvalds".
 *
 * Usernames may contain characters (spaces, accents, ...) that must be URL-encoded
 * to produce valid URLs.
 */
interface BuildProbeUrlUseCase {

    /**
     * Replaces the {} placeholder in [urlPattern] with the URL-encoded [username].
     */
    operator fun invoke(urlPattern: String, username: String): String
}