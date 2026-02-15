package com.madinaappstudio.recallmate.mcq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.madinaappstudio.recallmate.mcq.repository.McqRepository

class McqViewModelFactory(
    private val repository: McqRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(McqViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return McqViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}