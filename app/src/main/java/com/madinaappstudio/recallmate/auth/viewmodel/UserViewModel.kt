package com.madinaappstudio.recallmate.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madinaappstudio.recallmate.auth.repository.UserRepository
import com.madinaappstudio.recallmate.core.models.UserModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UserUiEvent>()
    val uiEvent: SharedFlow<UserUiEvent> = _uiEvent

    fun loadUser(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        repo.fetchUser(userId) { result ->
            result
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(error.message ?: "Failed to load user")
                }
        }
    }

    fun saveUser(userId: String, user: UserModel) {
        repo.setUser(userId, user) { result ->
            result
                .onSuccess {
                    emitSuccess("Signed up successfully", userId)
                }
                .onFailure { error ->
                    emitError(error.message ?: "Failed to signed up")
                }
        }
    }

    fun updateUser(userId: String, user: UserModel) {
        repo.setUser(userId, user) { result ->
            result
                .onSuccess {
                    emitSuccess("Profile updated successfully", userId)
                }
                .onFailure { error ->
                    emitError(error.message ?: "Failed to update")
                }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UserUiEvent.Error(message))
        }
    }

    private fun emitSuccess(message: String, userId: String) {
        viewModelScope.launch {
            _uiEvent.emit(UserUiEvent.Success(message, userId))
        }
    }
}

