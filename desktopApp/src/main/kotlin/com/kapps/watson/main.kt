package com.kapps.watson

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kapps.watson.presentation.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Watson",
    ) {
        App()
    }
}