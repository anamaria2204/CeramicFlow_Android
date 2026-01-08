package com.example.ceramicflow_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ceramicflow_android.CeramicFlowApplication

/**
 * A worker that runs annually to clean up the local database cache.
 */
class AnnualCleanupWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CeramicFlowApplication
        val ceramicItemDao = app.database.ceramicItemDao()

        return try {
            ceramicItemDao.deleteAllItems()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "com.example.ceramicflow_android.workers.AnnualCleanupWorker"
    }
}