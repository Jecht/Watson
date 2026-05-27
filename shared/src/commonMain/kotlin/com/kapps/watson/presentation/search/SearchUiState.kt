package com.kapps.watson.presentation.search

import com.kapps.watson.core.model.QueryResult
import com.kapps.watson.core.model.QueryStatus

/**
 * Immutable state for the search screen.
 *
 * The UI never reads the ViewModel directly — only this state. Any change must
 * produce a new instance (use [copy]) so Compose can detect it and recompose.
 */
data class SearchUiState(
    val usernameInput: String = "",
    val isScanning: Boolean = false,
    val results: List<QueryResult> = emptyList(),
    val errorMessage: String? = null,
    val totalSites: Int = 0,
) {
    /** Number of sites that have been probed so far during the current scan. */
    val probedCount: Int = results.size

    /** Subset of results where the username was found. */
    val claimedResults: List<QueryResult> = results.filter { result ->
        result.status == QueryStatus.CLAIMED
    }

    /** True when the search button should be enabled. */
    val canStartScan: Boolean = usernameInput.isNotBlank() && isScanning.not()
}