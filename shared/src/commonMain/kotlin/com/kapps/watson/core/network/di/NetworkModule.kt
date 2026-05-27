package com.kapps.watson.core.network.di

import com.kapps.watson.core.domain.network.SiteProbeService
import com.kapps.watson.core.network.*
import org.koin.dsl.module

/**
 * Koin module that provides networking primitives:
 * a shared Json parser and a shared HttpClient.
 *
 * Both are declared as singles since they are stateless, expensive to instantiate,
 * and intended to be shared across the whole application lifecycle.
 */
val networkModule = module {
    single { createJson() }
    single { createHttpClient(json = get()) }
    single<SiteProbeService> { SiteProbeServiceImpl(httpClient = get()) }
    single<ExclusionsService> { ExclusionsServiceImpl(httpClient = get()) }
}