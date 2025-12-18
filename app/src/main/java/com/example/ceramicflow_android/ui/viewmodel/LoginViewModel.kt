package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.LoginRequest
import com.example.ceramicflow_android.data.model.User
import com.example.ceramicflow_android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Correctly get the repository from the Application class
    private val authRepository: AuthRepository = (application as CeramicFlowApplication).authRepository

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Username and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            // The login function in the repository now handles token saving
            val result = authRepository.login(LoginRequest(username, password))

            result.onSuccess { loginResponse ->
                // The token is already saved. Just update the UI state.
                _uiState.value = LoginUiState.Success(loginResponse.user)
            }.onFailure { exception ->
                _uiState.value = LoginUiState.Error(
                    exception.message ?: "Login failed"
                )
            }
        }
    }

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Username and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = authRepository.register(LoginRequest(username, password))

            result.onSuccess { loginResponse ->
                // Register automatically logs the user in.
                _uiState.value = LoginUiState.Success(loginResponse.user)
            }.onFailure { exception ->
                _uiState.value = LoginUiState.Error(
                    exception.message ?: "Registration failed"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
