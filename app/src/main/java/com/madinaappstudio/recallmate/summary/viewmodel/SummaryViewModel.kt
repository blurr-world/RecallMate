package com.madinaappstudio.recallmate.summary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madinaappstudio.recallmate.core.models.SummaryModel
import com.madinaappstudio.recallmate.summary.repository.SummaryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.onSuccess

class SummaryViewModel(
    private val repository: SummaryRepository
) : ViewModel() {

    val selectedSummary = MutableStateFlow<SummaryModel?>(null)

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<SummaryUiEvent>()
    val uiEvent: SharedFlow<SummaryUiEvent> = _uiEvent

    fun loadAllSummary(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        repository.fetchAllSummary(userId) { result ->
            result
                .onSuccess { summaryList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summaryList = summaryList
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(throwable.message ?: "Failed to load summary")
                }
        }
    }

    fun loadSummary(userId: String, summaryId: String) {
        _uiState.update { it.copy(isLoading = true) }

        repository.fetchSummary(userId, summaryId) { result ->
            result
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(throwable.message ?: "Failed to load a summary")
                }
        }
    }

    fun saveSummary(userId: String, summary: SummaryModel) {
        repository.setSummary(userId, summary) { result ->
            result
                .onSuccess {
                    emitSuccess("Summary saved successfully")
                }
                .onFailure {
                    emitError(it.message ?: "Save failed")
                }
        }
    }

    fun removeSummary(userId: String, summaryId: String) {
        repository.deleteSummary(userId, summaryId) { result ->
            result
                .onSuccess {
                    emitSuccess("Summary deleted")
                }
                .onFailure {
                    emitError("Failed to delete summary")
                }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(SummaryUiEvent.Error(message))
        }
    }

    private fun emitSuccess(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(SummaryUiEvent.Success(message))
        }
    }
}
