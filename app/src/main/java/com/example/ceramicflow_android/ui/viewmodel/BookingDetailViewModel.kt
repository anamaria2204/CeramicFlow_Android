package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.repository.CeramicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// TODO: Rename this class to CeramicDetailViewModel for clarity
class CeramicDetailViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val ceramicId: String = savedStateHandle.get<String>("ceramicId")!!
    private val ceramicRepository: CeramicRepository

    // The UI state is now a direct, reactive flow from the database
    val uiState: StateFlow<UiState<CeramicItem>>

    init {
        val app = application as CeramicFlowApplication
        ceramicRepository = app.ceramicRepository

        uiState = ceramicRepository.getCeramicById(ceramicId)
            .map { ceramic ->
                if (ceramic != null) {
                    UiState.Success(ceramic)
                } else {
                    UiState.Error("Ceramic not found or not yet synced.")
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )
    }
}