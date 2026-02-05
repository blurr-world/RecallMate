package com.madinaappstudio.recallmate.flashcard.viewmodel

sealed class FlashcardUiEvent {
    data class Error(val message: String) : FlashcardUiEvent()
    data class Success(val message: String) : FlashcardUiEvent()
}
