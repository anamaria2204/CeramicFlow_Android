package com.example.ceramicflow_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ceramicflow_android.CeramicFlowApplication

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CeramicFlowApplication
        val repository = app.ceramicRepository
        val notificationHelper = app.notificationHelper

        return try {
            repository.refreshCeramicItems()
            notificationHelper.showSyncCompleteNotification()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "com.example.ceramicflow_android.workers.SyncWorker"
    }
}