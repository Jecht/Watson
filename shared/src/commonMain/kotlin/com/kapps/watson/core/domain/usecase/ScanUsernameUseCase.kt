package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.QueryResult
import kotlinx.coroutines.flow.Flow

/**
 * Scans every supported site for the given [username] and streams [QueryResult]s as they arrive.
 *
 * The scan is performed concurrently with a bounded parallelism — too many simultaneous
 * connections trigger socket exhaustion on iOS and may look like a DoS to upstream hosts.
 * Results are emitted on a [Flow] so consumers can render them progressively.
 *
 * Port of the Sherlock orchestration loop (the `sherlock()` function in sherlock.py).
 */
interface ScanUsernameUseCase {

    /**
     * Returns a cold [Flow] that, once collected, scans every supported site for [username]
     * and emits one [QueryResult] per probed site.
     *
     * @param username The username to look up across sites.
     * @return A flow of results, completing once every site has been probed.
     */
    suspend operator fun invoke(username: String): Flow<QueryResult>
}