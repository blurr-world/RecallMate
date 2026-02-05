package com.madinaappstudio.recallmate.core.models

data class FlashcardModel(
    var id: String = "",
    var groupId: String = "",
    var question: String = "",
    var answer: String = "",
    var completed: Boolean = false
)