package com.madinaappstudio.recallmate.flashcard.viewmodel

import com.madinaappstudio.recallmate.core.models.FlashcardModel
import com.madinaappstudio.recallmate.flashcard.model.FlashcardSetItem

data class FlashcardUiState(
    val isLoading: Boolean = false,
    val flashcardSets: List<FlashcardSetItem> = emptyList(),
    val flashcards: List<FlashcardModel> = emptyList()
)
