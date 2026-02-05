package com.madinaappstudio.recallmate.flashcard.model

data class FlashcardSetItem(
    var id: String = "",
    var title: String = "",
    var totalCards: String = "",
    var createdAt: Long = System.currentTimeMillis()
)
