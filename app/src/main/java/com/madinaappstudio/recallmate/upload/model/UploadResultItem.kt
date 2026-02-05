package com.madinaappstudio.recallmate.upload.model

sealed class UploadResultItem {
    object SectionDivider : UploadResultItem()
    data class SectionHeader(
        val title: String
    ) : UploadResultItem()

    data class Summary(
        val text: String
    ) : UploadResultItem()

    data class Flashcard(
        val question: String,
        val answer: String,
        var expanded: Boolean = false
    ) : UploadResultItem()
}