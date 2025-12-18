package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.repository.AuthRepository
import com.example.ceramicflow_android.data.repository.CeramicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class CeramicDetailUiState {
    object Loading : CeramicDetailUiState()
    data class Success(val ceramic: CeramicItem) : CeramicDetailUiState()
    data class Error(val message: String) : CeramicDetailUiState()
}

// TODO: Rename this class to CeramicDetailViewModel for clarity
class CeramicDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val ceramicRepository: CeramicRepository
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow<CeramicDetailUiState>(CeramicDetailUiState.Loading)
    val uiState: StateFlow<CeramicDetailUiState> = _uiState.asStateFlow()

    init {
        val app = application as CeramicFlowApplication
        ceramicRepository = app.ceramicRepository
        authRepository = app.authRepository
    }

    fun loadCeramic(ceramicId: String) {
        viewModelScope.launch {
            _uiState.value = CeramicDetailUiState.Loading
            try {
                val user = authRepository.getLoggedInUser()
                if (user == null) {
                    _uiState.value = CeramicDetailUiState.Error("User not logged in")
                    return@launch
                }

                // Find the specific item from the correct flow based on user role
                val ceramic = (if (user.isAdmin) {
                    ceramicRepository.getAllItems()
                } else {
                    ceramicRepository.getItemsForUser(user.id)
                }).firstOrNull()?.find { it.id == ceramicId }

                if (ceramic != null) {
                    _uiState.value = CeramicDetailUiState.Success(ceramic)
                } else {
                    _uiState.value = CeramicDetailUiState.Error("Ceramic not found or not accessible")
                }
            } catch (e: Exception) {
                _uiState.value = CeramicDetailUiState.Error(
                    e.message ?: "Failed to load ceramic details"
                )
            }
        }
    }
}