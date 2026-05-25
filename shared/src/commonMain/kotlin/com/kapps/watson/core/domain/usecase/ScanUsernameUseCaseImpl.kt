package com.kapps.watson.core.domain.usecase

import com.kapps.watson.core.model.QueryResult
import com.kapps.watson.core.repository.SitesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class ScanUsernameUseCaseImpl(
    private val sitesRepository: SitesRepository,
    private val probeSiteUseCase: ProbeSiteUseCase,
) : ScanUsernameUseCase {
    /**
     * Maximum number of concurrent probes in flight at any time.
     *
     * This bound is borrowed from Sherlock (max_workers=20 in sherlock.py): high enough
     * to keep a typical scan under two minutes, low enough to avoid socket exhaustion
     * on iOS (NSURLSession enforces stricter per-host limits than OkHttp).
     */
    private val maxConcurrency = 20

    override suspend fun invoke(username: String): Flow<QueryResult> = channelFlow {
        val sites = sitesRepository.loadSites()
        val semaphore = Semaphore(permits = maxConcurrency)

        coroutineScope {
            val jobs = sites.map { (siteName, site) ->
                async {
                    semaphore.withPermit {
                        probeSiteUseCase(
                            username = username,
                            siteName = siteName,
                            site = site,
                        )
                    }
                }
            }

            jobs.forEach { job ->
                val result = job.await()
                send(result)
            }
        }
    }.flowOn(Dispatchers.Default)
}