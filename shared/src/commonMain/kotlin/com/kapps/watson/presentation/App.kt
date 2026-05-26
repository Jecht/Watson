package com.kapps.watson.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kapps.watson.presentation.search.SearchScreen

/**
 * Root composable for the Watson application.
 * Hosts the MaterialTheme and the navigation graph (single screen for now).
 */
@Composable
fun App() {
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Watson",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
            },
            content = { paddingValues ->
                SearchScreen(modifier = Modifier.padding(paddingValues = paddingValues))
            },
        )
    }
}