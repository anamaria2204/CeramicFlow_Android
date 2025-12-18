package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.db.CeramicItemDao
import com.example.ceramicflow_android.data.model.CeramicItem
import kotlinx.coroutines.flow.Flow

class CeramicRepository(private val ceramicItemDao: CeramicItemDao, private val apiService: ApiService) {

    // This function now fetches from the REAL server and updates the local database.
    suspend fun refreshCeramicItems() {
        try {
            // Fetch from the network
            val networkItems = apiService.getCeramics()
            // Save the result to the local database
            ceramicItemDao.insertAll(networkItems)
        } catch (e: Exception) {
            // Handle exceptions, e.g., network down, server error
            // For now, we just let it fail, but you could add logging or error handling.
            throw e
        }
    }

    // The rest of the app reads from the database, which is the Single Source of Truth

    fun getItemsForUser(userId: String): Flow<List<CeramicItem>> {
        return ceramicItemDao.getItemsByUserId(userId)
    }

    fun getAllItems(): Flow<List<CeramicItem>> {
        return ceramicItemDao.getAllItems()
    }
}