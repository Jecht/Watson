package com.kapps.watson.core.network.di

import com.kapps.watson.core.network.createHttpClient
import com.kapps.watson.core.network.createJson
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
}