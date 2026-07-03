package com.kapps.watson

import com.kapps.watson.di.sharedModules
import com.kapps.watson.presentation.di.presentationModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

/**
 * Starts Koin for the iOS app.
 *
 * Android does this in [WatsonApplication] and desktop in main(); iOS had no such entry point,
 * so the first `koinViewModel()` call crashed with "KoinApplication has not been started".
 * Call this once from the SwiftUI `App` initializer.
 *
 * Idempotent: a no-op when Koin is already running (e.g. the view controller is recreated).
 */
fun startKoinApp() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        modules(sharedModules + presentationModule)
    }
}
