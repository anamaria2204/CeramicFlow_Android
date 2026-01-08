package com.example.ceramicflow_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ceramicflow_android.CeramicFlowApplication
import com.example.ceramicflow_android.data.model.BookingRequest
import com.example.ceramicflow_android.data.model.NewCeramicData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadBookingWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CeramicFlowApplication
        val bookingDao = app.database.bookingDao()
        val bookingRepository = app.bookingRepository
        val notificationHelper = app.notificationHelper

        val unsyncedBookings = bookingDao.getUnsyncedBookings()

        if (unsyncedBookings.isEmpty()) {
            return Result.success() // Nothing to do
        }

        return withContext(Dispatchers.IO) {
            try {
                var successfulUploads = 0
                unsyncedBookings.forEach { localBooking ->
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

                    // --- LINIA CORECTATĂ ---
                    // Apelăm funcția publică din repository, nu componenta internă.
                    val response = bookingRepository.syncOfflineBooking(request)

                    if (response.isSuccessful && response.body() != null) {
                        val serverBooking = response.body()!!
                        bookingDao.insertBooking(serverBooking.copy(isSynced = true))
                        bookingDao.deleteBookingsByIds(listOf(localBooking.id))
                        successfulUploads++
                    }
                }

                if (successfulUploads > 0) {
                    notificationHelper.showUploadSuccessNotification(successfulUploads)
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry() // If it fails, retry later
            }
        }
    }

    companion object {
        const val WORK_NAME = "com.example.ceramicflow_android.workers.UploadBookingWorker"
    }
}