package com.kapps.watson

import android.app.Application
import com.kapps.watson.di.sharedModules
import com.kapps.watson.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WatsonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WatsonApplication)
            modules(sharedModules + presentationModule)
        }
    }
}