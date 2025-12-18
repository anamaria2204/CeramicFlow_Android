package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.repository.AuthRepository
import com.example.ceramicflow_android.data.repository.CeramicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CeramicListUiState {
    object Loading : CeramicListUiState()
    data class Success(val ceramics: List<CeramicItem>) : CeramicListUiState()
    data class Error(val message: String) : CeramicListUiState()
}

class BookingListViewModel(application: Application) : AndroidViewModel(application) {

    private val ceramicRepository: CeramicRepository
    private val authRepository: AuthRepository

    private val _uiState = MutableStateFlow<CeramicListUiState>(CeramicListUiState.Loading)
    val uiState: StateFlow<CeramicListUiState> = _uiState.asStateFlow()

    init {
        val app = application as CeramicFlowApplication
        ceramicRepository = app.ceramicRepository
        authRepository = app.authRepository
        loadCeramics()
    }

    private fun loadCeramics() {
        viewModelScope.launch {
            _uiState.value = CeramicListUiState.Loading
            try {
                ceramicRepository.refreshCeramicItems()

                authRepository.getLoggedInUser()?.let { user ->
                    val ceramicsFlow = if (user.isAdmin) {
                        ceramicRepository.getAllItems()
                    } else {
                        ceramicRepository.getItemsForUser(user.id)
                    }

                    ceramicsFlow.collect { ceramics ->
                        _uiState.value = CeramicListUiState.Success(ceramics)
                    }
                } ?: run {
                    _uiState.value = CeramicListUiState.Error("User not logged in")
                }
            } catch (e: Exception) {
                _uiState.value = CeramicListUiState.Error(
                    e.message ?: "Failed to load ceramics"
                )
            }
        }
    }
}
