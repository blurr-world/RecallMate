package com.madinaappstudio.recallmate.core.models

data class SummaryModel(
    var id: String = "",
    var title: String = "",
    var summary: String = "",
    var length: String = "",
    var audienceLevel: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    val sourceTitle: String = ""
)