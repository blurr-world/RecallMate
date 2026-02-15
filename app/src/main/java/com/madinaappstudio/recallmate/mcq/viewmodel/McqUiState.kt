package com.madinaappstudio.recallmate.mcq.viewmodel

import com.madinaappstudio.recallmate.mcq.model.McqSetItem

data class McqUiState(
    val isLoading: Boolean = false,
    val mcqList: List<McqSetItem> = emptyList(),
    val mcqSet: McqSetItem? = null
)