package com.kapps.watson.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kapps.watson.core.model.QueryResult
import com.kapps.watson.core.platform.openUrlInBrowser
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.usernameInput,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("Username") },
                placeholder = { Text("e.g. torvalds") },
                enabled = !state.isScanning,
                singleLine = true,
            )

            if (state.isScanning) {
                OutlinedButton(
                    onClick = viewModel::onCancelScan,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Cancel")
                }
            } else {
                Button(
                    onClick = viewModel::onStartScan,
                    enabled = state.canStartScan,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Search")
                }
            }

            if (state.isScanning) {
                ScanProgress(
                    probedCount = state.probedCount,
                    totalSites = state.totalSites,
                )
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (state.claimedResults.isNotEmpty()) {
                Text(
                    text = "Found ${state.claimedResults.size} accounts",
                    style = MaterialTheme.typography.titleMedium,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.claimedResults,
                        key = { result -> result.siteName },
                    ) { result ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                                    slideInVertically(
                                        animationSpec = tween(durationMillis = 300),
                                        initialOffsetY = { fullHeight -> fullHeight / 2 },
                                    ),
                        ) {
                            ClaimedSiteCard(result = result)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ScanProgress(probedCount: Int, totalSites: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (totalSites > 0) {
            // Progress is known: show a determinate bar.
            LinearProgressIndicator(
                progress = { probedCount.toFloat() / totalSites.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "$probedCount / $totalSites sites probed",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            // Total unknown yet: fall back to an indeterminate bar.
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = "$probedCount sites probed",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ClaimedSiteCard(result: QueryResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openUrlInBrowser(result.siteUrl) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = result.siteName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = result.siteUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
