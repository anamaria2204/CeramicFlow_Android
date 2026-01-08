package com.example.ceramicflow_android

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.coroutineScope
import androidx.work.*
import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.api.RetrofitClient
import com.example.ceramicflow_android.data.datastore.UserPreferencesRepository
import com.example.ceramicflow_android.data.db.CeramicFlowDatabase
import com.example.ceramicflow_android.data.repository.AuthRepository
import com.example.ceramicflow_android.data.repository.BookingRepository
import com.example.ceramicflow_android.data.repository.CeramicRepository
import com.example.ceramicflow_android.util.NetworkStatusMonitor
import com.example.ceramicflow_android.util.NotificationHelper
import com.example.ceramicflow_android.workers.AnnualCleanupWorker
import com.example.ceramicflow_android.workers.SyncWorker
import com.example.ceramicflow_android.workers.UploadBookingWorker
import com.example.ceramicflow_android.workers.WeeklyRefreshWorker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class CeramicFlowApplication : Application(), LifecycleOwner {

    val database: CeramicFlowDatabase by lazy { CeramicFlowDatabase.getDatabase(this) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    private val apiService: ApiService by lazy { RetrofitClient.instance }
    val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), userPreferencesRepository, apiService)
    }
    val ceramicRepository: CeramicRepository by lazy {
        CeramicRepository(database.ceramicItemDao(), apiService)
    }
    val bookingRepository: BookingRepository by lazy {
        BookingRepository(apiService, database.bookingDao(), database.ceramicItemDao(), notificationHelper)
    }

    val networkStatusMonitor: NetworkStatusMonitor by lazy { NetworkStatusMonitor(this) }

    override val lifecycle: Lifecycle = LifecycleRegistry(this)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
        scheduleBackgroundTasks()
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        networkStatusMonitor.isOnline
            .onEach { isOnline ->
                if (!isOnline) {
                    notificationHelper.showOfflineNotification()
                } else {
                    notificationHelper.showOnlineNotification()
                }
            }
            .launchIn(lifecycle.coroutineScope)
    }

    private fun scheduleBackgroundTasks() {
        val workManager = WorkManager.getInstance(this)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        // Periodic data refresh tasks
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(SyncWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, periodicSyncRequest)

        val weeklyRefreshRequest = PeriodicWorkRequestBuilder<WeeklyRefreshWorker>(7, TimeUnit.DAYS).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(WeeklyRefreshWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, weeklyRefreshRequest)

        val annualCleanupRequest = PeriodicWorkRequestBuilder<AnnualCleanupWorker>(365, TimeUnit.DAYS).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(AnnualCleanupWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, annualCleanupRequest)

        // Offline booking upload task
        val uploadConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val periodicUploadRequest = PeriodicWorkRequestBuilder<UploadBookingWorker>(1, TimeUnit.HOURS) // Retry every hour
            .setConstraints(uploadConstraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UploadBookingWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, periodicUploadRequest
        )

        // Also trigger an immediate upload check on app start
        val immediateUploadRequest = OneTimeWorkRequestBuilder<UploadBookingWorker>()
            .setConstraints(uploadConstraints)
            .build()
        workManager.enqueue(immediateUploadRequest)
    }
}