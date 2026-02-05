package com.madinaappstudio.recallmate.flashcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madinaappstudio.recallmate.core.models.FlashcardModel
import com.madinaappstudio.recallmate.flashcard.model.FlashcardSetItem
import com.madinaappstudio.recallmate.flashcard.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val repository: FlashcardRepository
) : ViewModel() {

    val flashcardSet = MutableStateFlow<FlashcardSetItem?>(null)
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<FlashcardUiEvent>()
    val uiEvent: SharedFlow<FlashcardUiEvent> = _uiEvent

    fun loadFlashcardSets(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        repository.fetchFlashcardSets(userId) { result ->
            result
                .onSuccess { sets ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            flashcardSets = sets
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(error.message ?: "Failed to load flashcard groups")
                }
        }
    }

    fun loadFlashcardsBySet(userId: String, flashcardSetId: String) {
        _uiState.update { it.copy(isLoading = true) }

        repository.fetchFlashcardsBySet(userId, flashcardSetId) { result ->
            result
                .onSuccess { cards ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            flashcards = cards
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(error.message ?: "Failed to load flashcards")
                }
        }
    }


    fun saveFlashcardSet(
        userId: String,
        title: String,
        cards: List<FlashcardModel>
    ) {
        repository.saveFlashcardSetWithCards(userId, title, cards) { result ->
            result
                .onSuccess {
                    emitSuccess("Flashcard group saved")
                }
                .onFailure { error ->
                    emitError(error.message ?: "Failed to save flashcard group")
                }
        }
    }

    fun updateFlashcard(
        userId: String,
        flashcardModel: FlashcardModel
    ) {
        repository.updateFlashcard(userId, flashcardModel) { result ->
            result
                .onSuccess {
                    emitSuccess("Flashcard updated")
                }
                .onFailure {
                    emitError("Failed to update flashcard")
                }
        }
    }

    fun removeFlashcardSet(
        userId: String,
        setId: String
    ) {
        repository.deleteFlashcardSet(userId, setId) { result ->
            result
                .onSuccess {
                    emitSuccess("Flashcard updated")
                }
                .onFailure {
                    emitError("Failed to update flashcard")
                }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(FlashcardUiEvent.Error(message))
        }
    }

    private fun emitSuccess(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(FlashcardUiEvent.Success(message))
        }
    }
}

