package com.madinaappstudio.recallmate.mcq.viewmodel

sealed class McqUiEvent {
    data class Error(val message: String) : McqUiEvent()
    data class Success(val message: String) : McqUiEvent()
}