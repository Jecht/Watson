package com.kapps.watson.di

import com.kapps.watson.core.network.di.networkModule
import com.kapps.watson.core.repository.di.repositoryModule

/**
 * List of all Koin modules used by the application.
 * Add new modules here as they are introduced.
 *
 * This list is consumed by the startKoin block in each platform's entry point.
 */
val appModules = listOf(
    networkModule,
     repositoryModule,
    // engineModule,       // added with the scan engine
    // viewModelModule,    // added with the UI
)