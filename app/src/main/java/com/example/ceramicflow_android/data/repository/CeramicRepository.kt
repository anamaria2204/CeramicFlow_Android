package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.db.CeramicItemDao
import com.example.ceramicflow_android.data.model.CeramicItem
import kotlinx.coroutines.flow.Flow

class CeramicRepository(private val ceramicItemDao: CeramicItemDao, private val apiService: ApiService) {

    // --- Funcția nouă pentru a observa un singur obiect ---
    fun getCeramicById(id: String): Flow<CeramicItem?> {
        return ceramicItemDao.getItemById(id)
    }

    suspend fun refreshCeramicItems() {
        try {
            val networkItems = apiService.getCeramics()
            ceramicItemDao.insertAll(networkItems)
        } catch (e: Exception) {
            // Handle network errors
        }
    }

    fun getItemsForUser(userId: String): Flow<List<CeramicItem>> {
        return ceramicItemDao.getItemsByUserId(userId)
    }

    fun getAllItems(): Flow<List<CeramicItem>> {
        return ceramicItemDao.getAllItems()
    }
}