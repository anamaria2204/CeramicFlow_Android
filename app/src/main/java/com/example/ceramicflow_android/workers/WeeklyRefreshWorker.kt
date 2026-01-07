package com.example.ceramicflow_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ceramicflow_android.CeramicFlowApplication

/**
 * A worker that runs weekly to refresh the ceramic items data from the server.
 * Its logic is identical to SyncWorker but is scheduled on a different interval.
 */
class WeeklyRefreshWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CeramicFlowApplication
        val repository = app.ceramicRepository

        return try {
            repository.refreshCeramicItems()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "com.example.ceramicflow_android.workers.WeeklyRefreshWorker"
    }
}