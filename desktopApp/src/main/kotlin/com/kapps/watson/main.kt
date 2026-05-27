package com.kapps.watson

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kapps.watson.di.sharedModules
import com.kapps.watson.presentation.App
import com.kapps.watson.presentation.di.presentationModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(sharedModules + presentationModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Watson",
        ) {
            App()
        }
    }
}