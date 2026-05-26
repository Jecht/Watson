package com.kapps.watson.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapps.watson.core.domain.usecase.ScanUsernameUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the search screen.
 *
 * Owns the [SearchUiState] and exposes it as a [StateFlow]. The actual username lookup
 * is delegated to [ScanUsernameUseCase], whose streamed results are accumulated
 * into the state as they arrive.
 */
class SearchViewModel(
    private val scanUsernameUseCase: ScanUsernameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun onUsernameChange(newValue: String) {
        _uiState.update { state ->
            state.copy(usernameInput = newValue, errorMessage = null)
        }
    }

    fun onStartScan() {
        val username = _uiState.value.usernameInput.trim()
        if (username.isEmpty() || _uiState.value.isScanning) return

        scanJob?.cancel()

        _uiState.update { state ->
            state.copy(
                isScanning = true,
                results = emptyList(),
                errorMessage = null,
            )
        }

        scanJob = viewModelScope.launch {
            scanUsernameUseCase(username = username)
                .onEach { result ->
                    _uiState.update { state -> state.copy(results = state.results + result) }
                }
                .catch { error ->
                    _uiState.update { state ->
                        state.copy(errorMessage = error.message ?: "Unknown error")
                    }
                }
                .onCompletion {
                    _uiState.update { state -> state.copy(isScanning = false) }
                }
                .collect()
        }
    }

    fun onCancelScan() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update { state -> state.copy(isScanning = false) }
    }
}