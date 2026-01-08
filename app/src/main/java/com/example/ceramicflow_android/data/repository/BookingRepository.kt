package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.db.BookingDao
import com.example.ceramicflow_android.data.db.CeramicItemDao
import com.example.ceramicflow_android.data.model.Booking
import com.example.ceramicflow_android.data.model.BookingRequest
import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.model.NewCeramicData
import com.example.ceramicflow_android.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class BookingRepository(
    private val apiService: ApiService,
    private val bookingDao: BookingDao,
    private val ceramicItemDao: CeramicItemDao,
    private val notificationHelper: NotificationHelper
) {

    fun getBookings(): Flow<List<Booking>> {
        return bookingDao.getAllBookings()
    }

    suspend fun refreshBookings() {
        try {
            try {
                syncPendingBookings()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val serverBookings = apiService.getBookings()
            val syncedBookings = serverBookings.map { it.copy(isSynced = true) }

            val ceramics = syncedBookings.mapNotNull { it.ceramic }

            if (ceramics.isNotEmpty()) {
                ceramicItemDao.insertAll(ceramics)
            }
            if (syncedBookings.isNotEmpty()) {
                bookingDao.insertAllBookings(syncedBookings)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncPendingBookings() {
        val unsyncedBookings = bookingDao.getUnsyncedBookings()
        var syncedCount = 0 // Contor pentru programările reușite

        unsyncedBookings.forEach { localBooking ->
            try {
                // Safety check: Dacă ceramica lipsește local, ștergem booking-ul corupt
                if (localBooking.ceramic == null) {
                    bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                    return@forEach
                }

                val request = BookingRequest(
                    date = localBooking.date,
                    time = localBooking.time,
                    userId = localBooking.userId,
                    newCeramic = NewCeramicData(
                        name = localBooking.ceramic.name,
                        type = localBooking.ceramic.type.name,
                        quantity = localBooking.ceramic.quantity,
                        description = localBooking.ceramic.description
                    )
                )

                val response = apiService.createBooking(request)
                if (response.isSuccessful && response.body() != null) {
                    val serverBooking = response.body()!!

                    // 1. Curățăm datele vechi (temporare)
                    bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                    ceramicItemDao.deleteById(localBooking.ceramic.id)

                    // 2. Salvăm datele noi (sincronizate)
                    if (serverBooking.ceramic != null) {
                        ceramicItemDao.insertAll(listOf(serverBooking.ceramic.copy(userId = serverBooking.userId)))
                        bookingDao.insertBooking(serverBooking.copy(isSynced = true))

                        // Incrementăm contorul
                        syncedCount++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (syncedCount > 0) {
            notificationHelper.showSummarySyncNotification(syncedCount)
        }
    }

    suspend fun createBooking(bookingRequest: BookingRequest): Result<Booking> {
        val tempCeramic = bookingRequest.newCeramic.let {
            com.example.ceramicflow_android.data.model.CeramicItem(
                id = "temp_" + UUID.randomUUID().toString(),
                name = it.name,
                type = com.example.ceramicflow_android.data.model.CeramicType.valueOf(it.type),
                quantity = it.quantity,
                description = it.description,
                userId = bookingRequest.userId
            )
        }

        val localBooking = Booking(
            id = "local_" + UUID.randomUUID().toString(),
            date = bookingRequest.date,
            time = bookingRequest.time,
            userId = bookingRequest.userId,
            ceramic = tempCeramic,
            isSynced = false
        )

        // Salvăm LOCAL (Optimistic UI)
        ceramicItemDao.insertAll(listOf(tempCeramic))
        bookingDao.insertBooking(localBooking)

        return try {
            val response = apiService.createBooking(bookingRequest)
            if (response.isSuccessful && response.body() != null) {
                val serverBooking = response.body()!!

                if (serverBooking.ceramic != null) {
                    ceramicItemDao.insertAll(listOf(serverBooking.ceramic.copy(userId = serverBooking.userId)))
                    bookingDao.insertBooking(serverBooking.copy(isSynced = true))
                }

                bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                ceramicItemDao.deleteById(tempCeramic.id)

                Result.success(serverBooking)
            } else {
                bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                ceramicItemDao.deleteById(tempCeramic.id)
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            notificationHelper.showBookingSavedLocallyNotification()
            Result.failure(e)
        } catch (e: Exception) {
            bookingDao.deleteBookingsByIds(listOf(localBooking.id))
            ceramicItemDao.deleteById(tempCeramic.id)
            Result.failure(e)
        }
    }

    suspend fun syncOfflineBooking(bookingRequest: BookingRequest): Response<Booking> {
        return apiService.createBooking(bookingRequest)
    }


    suspend fun deleteBooking(booking: Booking): Result<Unit> {
        if (!booking.isSynced) {
            bookingDao.deleteBookingsByIds(listOf(booking.id))
            ceramicItemDao.deleteById(booking.ceramic.id)
            return Result.success(Unit)
        }

        return try {
            val response = apiService.deleteBooking(booking.id)
            if (response.isSuccessful) {
                bookingDao.deleteBookingsByIds(listOf(booking.id))
                ceramicItemDao.deleteById(booking.ceramic.id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete on server"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Cannot delete synced booking."))
        }
    }

    suspend fun updateCeramic(ceramic: CeramicItem) {
        bookingDao.updateCeramic(ceramic)
    }
}