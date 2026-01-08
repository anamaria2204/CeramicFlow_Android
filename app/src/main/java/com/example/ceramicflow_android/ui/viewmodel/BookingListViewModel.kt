package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.repository.BookingRepository
import com.example.ceramicflow_android.util.NetworkConnectivityObserver
import com.example.ceramicflow_android.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class BookingListViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository: BookingRepository
    private val notificationHelper: NotificationHelper
    private val networkObserver: NetworkConnectivityObserver // Monitorul de rețea

    private val _uiState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Booking>>> = _uiState.asStateFlow()

    init {
        val app = application as CeramicFlowApplication
        bookingRepository = app.bookingRepository
        notificationHelper = app.notificationHelper
        networkObserver = NetworkConnectivityObserver(application) // Inițializăm monitorul

        // 1. Colectăm datele din baza de date locală (Flow)
        viewModelScope.launch {
            bookingRepository.getBookings()
                .collect { bookings ->
                    _uiState.value = UiState.Success(bookings)
                }
        }

        viewModelScope.launch {
            networkObserver.networkStatus.collect { isConnected ->
                if (isConnected) {
                    try {
                        println("Internet detected: Triggering auto-refresh...")
                        bookingRepository.refreshBookings()
                    } catch (e: Exception) {
                    }
                }
            }
        }

        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            try {
                bookingRepository.refreshBookings()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to refresh bookings")
            }
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            val result = bookingRepository.deleteBooking(booking)
            result.onFailure { e ->
                println("Error deleting booking: ${e.message}")
            }
        }
    }

    fun addPhotoToBooking(bookingId: String, photoUri: String) {
        viewModelScope.launch {
            val currentBookings = (_uiState.value as? UiState.Success)?.data ?: return@launch
            val booking = currentBookings.find { it.id == bookingId } ?: return@launch

            val newImages = booking.ceramic.images + photoUri

            val updatedCeramic = booking.ceramic.copy(images = newImages)

            bookingRepository.updateCeramic(updatedCeramic)
            loadBookings()
        }
    }
}