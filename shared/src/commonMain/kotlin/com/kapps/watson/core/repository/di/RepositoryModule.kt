package com.kapps.watson.core.repository.di

import com.kapps.watson.core.repository.SitesRepository
import com.kapps.watson.core.repository.SitesRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<SitesRepository> { SitesRepositoryImpl(httpClient = get(), json = get()) }
}