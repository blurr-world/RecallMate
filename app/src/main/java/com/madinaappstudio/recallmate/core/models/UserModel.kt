package com.madinaappstudio.recallmate.core.models

data class UserModel(
    val uid: String = "",
    var name: String = "",
    val email: String = "",
    val countSummary: Int = 0,
    val countFlashcard: Int = 0,
    val countMaterial: Int = 0
)