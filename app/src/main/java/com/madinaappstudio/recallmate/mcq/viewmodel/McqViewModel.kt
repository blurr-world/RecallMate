package com.madinaappstudio.recallmate.mcq.viewmodel

import androidx.lifecycle.ViewModel
import com.madinaappstudio.recallmate.mcq.repository.McqRepository

class McqViewModel (
    private val repository: McqRepository
) : ViewModel() {
}