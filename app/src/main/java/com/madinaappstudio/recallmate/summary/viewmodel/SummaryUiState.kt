package com.madinaappstudio.recallmate.summary.viewmodel

import com.madinaappstudio.recallmate.core.models.SummaryModel


data class SummaryUiState(
    val isLoading: Boolean = false,
    val summary: SummaryModel? = null,
    val summaryList: List<SummaryModel> = emptyList()
)
