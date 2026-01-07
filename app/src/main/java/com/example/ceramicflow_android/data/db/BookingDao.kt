package com.example.ceramicflow_android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ceramicflow_android.data.model.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY date DESC, time DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookings(bookings: List<Booking>)

    @Query("SELECT * FROM bookings WHERE isSynced = 0")
    suspend fun getUnsyncedBookings(): List<Booking>

    @Query("DELETE FROM bookings WHERE id IN (:bookingIds)")
    suspend fun deleteBookingsByIds(bookingIds: List<String>)

    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()
}