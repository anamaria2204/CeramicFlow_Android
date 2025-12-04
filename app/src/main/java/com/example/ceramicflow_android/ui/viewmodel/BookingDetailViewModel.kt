package com.example.ceramicflow_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingDetailUiState {
    object Loading : BookingDetailUiState()
    data class Success(val booking: Booking) : BookingDetailUiState()
    data class Error(val message: String) : BookingDetailUiState()
}

class BookingDetailViewModel(
    private val dataRepository: MockDataRepository = MockDataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingDetailUiState>(BookingDetailUiState.Loading)
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = BookingDetailUiState.Loading
            try {
                val booking = dataRepository.getBookingById(bookingId)
                if (booking != null) {
                    _uiState.value = BookingDetailUiState.Success(booking)
                } else {
                    _uiState.value = BookingDetailUiState.Error("Booking not found")
                }
            } catch (e: Exception) {
                _uiState.value = BookingDetailUiState.Error(
                    e.message ?: "Failed to load booking"
                )
            }
        }
    }

    fun addItem(bookingId: String, item: CeramicItem) {
        viewModelScope.launch {
            try {
                val success = dataRepository.addItemToBooking(bookingId, item)
                if (success) {
                    _operationStatus.value = "Item added successfully"
                    loadBooking(bookingId) // Refresh the booking
                } else {
                    _operationStatus.value = "Failed to add item"
                }
            } catch (e: Exception) {
                _operationStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteItem(bookingId: String, itemId: String) {
        viewModelScope.launch {
            try {
                val success = dataRepository.deleteItemFromBooking(bookingId, itemId)
                if (success) {
                    _operationStatus.value = "Item deleted successfully"
                    loadBooking(bookingId) // Refresh the booking
                } else {
                    _operationStatus.value = "Failed to delete item"
                }
            } catch (e: Exception) {
                _operationStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }
}
