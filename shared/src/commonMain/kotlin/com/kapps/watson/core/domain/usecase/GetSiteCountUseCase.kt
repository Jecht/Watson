package com.kapps.watson.core.domain.usecase

/**
 * Returns the total number of sites in the catalog.
 *
 * Used by the UI to display scan progress as "X / total" rather than just "X".
 * NSFW sites are excluded by default, matching the default behavior of [ScanUsernameUseCase].
 */
interface GetSiteCountUseCase {
    suspend operator fun invoke(): Int
}