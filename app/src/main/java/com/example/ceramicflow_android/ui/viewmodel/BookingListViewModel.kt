package com.example.ceramicflow_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingListUiState {
    object Loading : BookingListUiState()
    data class Success(val bookings: List<Booking>) : BookingListUiState()
    data class Error(val message: String) : BookingListUiState()
}

class BookingListViewModel(
    private val dataRepository: MockDataRepository = MockDataRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingListUiState>(BookingListUiState.Loading)
    val uiState: StateFlow<BookingListUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = BookingListUiState.Loading
            try {
                val bookings = dataRepository.getBookings()
                _uiState.value = BookingListUiState.Success(bookings)
            } catch (e: Exception) {
                _uiState.value = BookingListUiState.Error(
                    e.message ?: "Failed to load bookings"
                )
            }
        }
    }
}
