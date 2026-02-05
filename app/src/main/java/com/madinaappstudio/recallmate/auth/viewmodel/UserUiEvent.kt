package com.madinaappstudio.recallmate.auth.viewmodel

sealed class UserUiEvent {
    data class Error(val message: String) : UserUiEvent()
    data class Success(val message: String, val userId: String) : UserUiEvent()
}
