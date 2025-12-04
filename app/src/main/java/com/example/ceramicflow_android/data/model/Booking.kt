package com.example.ceramicflow_android.data.model

data class Booking(
    val id: String,
    val clientName: String,
    val date: String,
    val timeSlot: String,
    val status: BookingStatus,
    val items: MutableList<CeramicItem> = mutableListOf()
)

enum class BookingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
