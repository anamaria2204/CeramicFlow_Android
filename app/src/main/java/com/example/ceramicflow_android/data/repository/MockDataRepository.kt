package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.BookingStatus
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.model.CeramicType
import kotlinx.coroutines.delay
import java.util.UUID

class MockDataRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: MockDataRepository? = null
        
        fun getInstance(): MockDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MockDataRepository().also { INSTANCE = it }
            }
        }
    }
    
    // Simulating a database with mock data
    private val bookings = mutableListOf(
        Booking(
            id = "1",
            clientName = "Maria Popescu",
            date = "2024-12-05",
            timeSlot = "10:00 - 12:00",
            status = BookingStatus.PENDING,
            items = mutableListOf(
                CeramicItem("i1", "Cana mare", CeramicType.MUG, 2, "Cu model floral"),
                CeramicItem("i2", "Ceasca cafea", CeramicType.COFFEE_CUP, 4, "Set espresso")
            )
        ),
        Booking(
            id = "2",
            clientName = "Ion Ionescu",
            date = "2024-12-06",
            timeSlot = "14:00 - 16:00",
            status = BookingStatus.IN_PROGRESS,
            items = mutableListOf(
                CeramicItem("i3", "Farfurie", CeramicType.PLATE, 6, "Diametru 25cm"),
                CeramicItem("i4", "Bol", CeramicType.BOWL, 3, "Pentru supa")
            )
        ),
        Booking(
            id = "3",
            clientName = "Ana Marinescu",
            date = "2024-12-07",
            timeSlot = "09:00 - 11:00",
            status = BookingStatus.PENDING,
            items = mutableListOf(
                CeramicItem("i5", "Vaza", CeramicType.VASE, 1, "Stil modern"),
                CeramicItem("i6", "Cana mica", CeramicType.MUG, 2, "Pentru copii")
            )
        ),
        Booking(
            id = "4",
            clientName = "Gheorghe Vasilescu",
            date = "2024-12-08",
            timeSlot = "15:00 - 17:00",
            status = BookingStatus.COMPLETED,
            items = mutableListOf(
                CeramicItem("i7", "Set cafea", CeramicType.COFFEE_CUP, 6, "Cu farfurioare"),
                CeramicItem("i8", "Cana mare", CeramicType.MUG, 1, "Personalizata")
            )
        ),
        Booking(
            id = "5",
            clientName = "Elena Dumitrescu",
            date = "2024-12-09",
            timeSlot = "11:00 - 13:00",
            status = BookingStatus.PENDING,
            items = mutableListOf(
                CeramicItem("i9", "Farfurii desert", CeramicType.PLATE, 8, "Diametru 20cm"),
                CeramicItem("i10", "Boluri cereale", CeramicType.BOWL, 4, "Design minimalist")
            )
        )
    )

    // Simulate network delay
    suspend fun getBookings(): List<Booking> {
        delay(500)
        return bookings.toList()
    }

    suspend fun getBookingById(bookingId: String): Booking? {
        delay(300)
        return bookings.find { it.id == bookingId }
    }

    suspend fun addItemToBooking(bookingId: String, item: CeramicItem): Boolean {
        delay(200)
        val booking = bookings.find { it.id == bookingId }
        return if (booking != null) {
            val newItem = item.copy(id = UUID.randomUUID().toString())
            booking.items.add(newItem)
            true
        } else {
            false
        }
    }

    suspend fun deleteItemFromBooking(bookingId: String, itemId: String): Boolean {
        delay(200)
        val booking = bookings.find { it.id == bookingId }
        return if (booking != null) {
            booking.items.removeIf { it.id == itemId }
            true
        } else {
            false
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Boolean {
        delay(200)
        val booking = bookings.find { it.id == bookingId }
        return if (booking != null) {
            val index = bookings.indexOf(booking)
            bookings[index] = booking.copy(status = status)
            true
        } else {
            false
        }
    }
}
