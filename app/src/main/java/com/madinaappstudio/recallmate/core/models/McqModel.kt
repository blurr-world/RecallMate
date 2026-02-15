package com.madinaappstudio.recallmate.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class McqModel(
    var id: String = "",
    var question: String = "",
    var options: List<String> = emptyList(),
    var correctOption: String = "",
) : Parcelable