package com.example.ceramicflow_android

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.api.RetrofitClient
import com.example.ceramicflow_android.data.datastore.UserPreferencesRepository
import com.example.ceramicflow_android.data.db.CeramicFlowDatabase
import com.example.ceramicflow_android.data.repository.AuthRepository
import com.example.ceramicflow_android.data.repository.CeramicRepository
import com.example.ceramicflow_android.util.NetworkStatusMonitor
import com.example.ceramicflow_android.util.NotificationHelper
import com.example.ceramicflow_android.workers.SyncWorker
import java.util.concurrent.TimeUnit

class CeramicFlowApplication : Application() {

    val database: CeramicFlowDatabase by lazy { CeramicFlowDatabase.getDatabase(this) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    private val apiService: ApiService by lazy { RetrofitClient.instance }

    // Inject all dependencies
    val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), userPreferencesRepository, apiService)
    }
    val ceramicRepository: CeramicRepository by lazy {
        CeramicRepository(database.ceramicItemDao(), apiService)
    }

    val networkStatusMonitor: NetworkStatusMonitor by lazy { NetworkStatusMonitor(this) }
    val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
}