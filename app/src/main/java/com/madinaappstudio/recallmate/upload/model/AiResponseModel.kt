package com.madinaappstudio.recallmate.upload.model

data class AiResponseModel(
    var summary: Summary,
    var flashcards: Flashcards?,
    var mcq: MCQ?
) {
    data class Summary(
        var summaryTitle: String,
        var summary: String,
        var sourceTitle: String,
        var summaryLength: String,
        var summaryAudienceLevel: String,
    )
    data class Flashcards(
        var flashcardTitle: String,
        var cards: List<Card>,
    ) {
        data class Card(
            var question: String,
            var answer: String
        )
    }

    data class MCQ(
        var question: String,
        var firstOption: String,
        var secondOption: String,
        var thirdOption: String,
        var fourthOption: String,
        var correctOption: String
    )
}