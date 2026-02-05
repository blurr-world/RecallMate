package com.madinaappstudio.recallmate.core.models

data class ChatModel (
    var id: Long = System.currentTimeMillis(),
    var text: String,
    var isUser: Boolean,
    val isTyping: Boolean,
)