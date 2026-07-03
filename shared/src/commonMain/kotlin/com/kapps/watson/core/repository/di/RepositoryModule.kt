package com.kapps.watson.core.repository.di

import com.kapps.watson.core.repository.CatalogStore
import com.kapps.watson.core.repository.DiskCatalogStore
import com.kapps.watson.core.repository.SitesRepository
import com.kapps.watson.core.repository.SitesRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<CatalogStore> { DiskCatalogStore() }
    single<SitesRepository> {
        SitesRepositoryImpl(
            httpClient = get(),
            json = get(),
            exclusionsService = get(),
            catalogStore = get(),
        )
    }
}