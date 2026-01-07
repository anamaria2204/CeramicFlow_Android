package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.repository.BookingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class BookingListViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository: BookingRepository

    private val _uiState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Booking>>> = _uiState.asStateFlow()

    init {
        val app = application as CeramicFlowApplication
        bookingRepository = app.bookingRepository

        // Observe the local database for changes and update the UI
        viewModelScope.launch {
            bookingRepository.getBookings()
                .collect { bookings ->
                    _uiState.value = UiState.Success(bookings)
                }
        }

        // Trigger a refresh from the server
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            try {
                // Don't show loading here, as the UI is already showing cached data
                bookingRepository.refreshBookings()
            } catch (e: Exception) {
                // If refresh fails, the UI will still show the cached data.
                _uiState.value = UiState.Error(e.message ?: "Failed to refresh bookings")
            }
        }
    }
}