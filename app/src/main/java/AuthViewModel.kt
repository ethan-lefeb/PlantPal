package com.example.plantpal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val username: String? = null
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = AuthRepository.registerUser(email, password, displayName)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(success = true)
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = AuthRepository.login(email, password)
            if (result.isSuccess) {
                val name = AuthRepository.getCurrentUserName()
                _uiState.value = AuthUiState(success = true, username = name)
            } else {
                _uiState.value = AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
