package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.db.BookingDao
import com.example.ceramicflow_android.data.db.CeramicItemDao
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.BookingRequest
import com.example.ceramicflow_android.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class BookingRepository(
    private val apiService: ApiService,
    private val bookingDao: BookingDao,
    private val ceramicItemDao: CeramicItemDao, // Adăugăm dependența lipsă
    private val notificationHelper: NotificationHelper
) {

    fun getBookings(): Flow<List<Booking>> {
        return bookingDao.getAllBookings()
    }

    // --- FUNCȚIA REPARATĂ ---
    suspend fun refreshBookings() {
        try {
            val serverBookings = apiService.getBookings()
            val syncedBookings = serverBookings.map { it.copy(isSynced = true) }

            // Extragem obiectele ceramice și le salvăm în tabela lor
            val ceramics = syncedBookings.map { it.ceramic }
            ceramicItemDao.insertAll(ceramics)

            // Salvăm programările
            bookingDao.insertAllBookings(syncedBookings)
        } catch (e: Exception) {
            // În caz de eroare de rețea, nu facem nimic. Aplicația va afișa datele din cache.
        }
    }

    suspend fun createBooking(bookingRequest: BookingRequest): Result<Booking> {
        val localBooking = Booking(
            id = "local_" + UUID.randomUUID().toString(),
            date = bookingRequest.date,
            time = bookingRequest.time,
            userId = bookingRequest.userId,
            ceramic = bookingRequest.newCeramic.let {
                com.example.ceramicflow_android.data.model.CeramicItem(
                    id = "temp_" + UUID.randomUUID().toString(),
                    name = it.name,
                    type = com.example.ceramicflow_android.data.model.CeramicType.valueOf(it.type),
                    quantity = it.quantity,
                    description = it.description,
                    userId = bookingRequest.userId
                )
            },
            isSynced = false
        )
        bookingDao.insertBooking(localBooking)

        return try {
            val response = apiService.createBooking(bookingRequest)
            if (response.isSuccessful && response.body() != null) {
                val serverBooking = response.body()!!
                bookingDao.insertBooking(serverBooking.copy(isSynced = true))
                bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                Result.success(serverBooking)
            } else {
                bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            notificationHelper.showBookingSavedLocallyNotification()
            Result.failure(e)
        } catch (e: Exception) {
            bookingDao.deleteBookingsByIds(listOf(localBooking.id))
            Result.failure(e)
        }
    }

    suspend fun syncOfflineBooking(bookingRequest: BookingRequest): Response<Booking> {
        return apiService.createBooking(bookingRequest)
    }
}