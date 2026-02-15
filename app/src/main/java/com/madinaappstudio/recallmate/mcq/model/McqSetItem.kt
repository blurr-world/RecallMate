package com.madinaappstudio.recallmate.mcq.model

import com.madinaappstudio.recallmate.core.models.McqModel

data class McqSetItem(
    var id: String = "",
    var mcqList: List<McqModel> = emptyList(),
    var totalMcq: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
)