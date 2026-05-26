package com.kapps.watson.presentation.di

import com.kapps.watson.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::SearchViewModel)
}