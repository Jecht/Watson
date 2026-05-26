package com.kapps.watson.di

import com.kapps.watson.core.domain.di.domainModule
import com.kapps.watson.core.network.di.networkModule
import com.kapps.watson.core.repository.di.repositoryModule

/**
 * List of all Koin modules used by the application.
 * Add new modules here as they are introduced.
 *
 * This list is consumed by the startKoin block in each platform's entry point.
 */
val sharedModules = listOf(
    networkModule,
    repositoryModule,
    domainModule,
    // viewModelModule,   // added with the UI
)