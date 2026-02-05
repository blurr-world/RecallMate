package com.madinaappstudio.recallmate.auth.viewmodel

import com.madinaappstudio.recallmate.core.models.UserModel

data class UserUiState(
    val isLoading: Boolean = false,
    val user: UserModel? = null
)
