package com.example.plantpal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantpal.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
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
}
