package com.example.ceramicflow_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.BookingRequest
import com.example.ceramicflow_android.data.model.NewCeramicData
import com.example.ceramicflow_android.data.repository.AuthRepository
import com.example.ceramicflow_android.data.repository.BookingRepository
import com.example.ceramicflow_android.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class AddBookingUiState {
    object Idle : AddBookingUiState()
    object Loading : AddBookingUiState()
    data class Success(val booking: Booking) : AddBookingUiState()
    data class Error(val message: String) : AddBookingUiState()
}

class AddBookingViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository: BookingRepository
    private val authRepository: AuthRepository
    private val notificationHelper: NotificationHelper

    private val _uiState = MutableStateFlow<AddBookingUiState>(AddBookingUiState.Idle)
    val uiState: StateFlow<AddBookingUiState> = _uiState.asStateFlow()

    private var tempNewCeramicData: NewCeramicData? = null

    init {
        val app = application as CeramicFlowApplication
        bookingRepository = app.bookingRepository
        authRepository = app.authRepository
        notificationHelper = app.notificationHelper
    }

    fun setCeramicData(newCeramicData: NewCeramicData) {
        tempNewCeramicData = newCeramicData
    }

    fun createBooking(date: String, time: String) {
        val ceramicData = tempNewCeramicData
        if (ceramicData == null) {
            _uiState.value = AddBookingUiState.Error("Ceramic data is missing.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddBookingUiState.Loading
            val user = authRepository.getLoggedInUser()
            if (user == null) {
                _uiState.value = AddBookingUiState.Error("User not logged in.")
                return@launch
            }

            val request = BookingRequest(date = date, time = time, userId = user.id, newCeramic = ceramicData)
            val result = bookingRepository.createBooking(request)

            result.onSuccess { booking ->
                _uiState.value = AddBookingUiState.Success(booking)
                notificationHelper.showBookingSuccessNotification(booking.date, booking.time)
            }.onFailure { exception ->
                when (exception) {
                    is IOException -> {
                        // Network error: Handled by a separate notification
                        // The UI will show an error, but the main feedback is the notification
                        _uiState.value = AddBookingUiState.Error("Network error: Saved locally.")
                    }
                    is HttpException -> {
                        // Server error (e.g., 409 Conflict)
                        _uiState.value = AddBookingUiState.Error("Server error: ${exception.message()}")
                    }
                    else -> {
                        // Other errors
                        _uiState.value = AddBookingUiState.Error(exception.message ?: "An unknown error occurred")
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AddBookingUiState.Idle
    }
}