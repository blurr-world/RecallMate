package com.madinaappstudio.recallmate.summary.viewmodel

sealed class SummaryUiEvent {
    data class Error(val message: String) : SummaryUiEvent()
    data class Success(val message: String) : SummaryUiEvent()
}
