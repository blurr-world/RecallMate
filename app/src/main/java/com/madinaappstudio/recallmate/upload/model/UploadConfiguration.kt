package com.madinaappstudio.recallmate.upload.model

data class UploadConfiguration(
    var summaryTitle: String? = null,
    var summaryLength: String = "Medium",
    var summaryAudienceLevel: String = "Intermediate",
    var isFlashcard: Boolean = false,
    var flashcardTitle: String? = null,
    var flashcardCount: Int = 5
)