package com.example.ceramicflow_android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ceramicflow_android.data.model.CeramicItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CeramicItemDao {

    @Query("SELECT * FROM ceramic_items")
    fun getAllItems(): Flow<List<CeramicItem>>

    @Query("SELECT * FROM ceramic_items WHERE userId = :userId")
    fun getItemsByUserId(userId: String): Flow<List<CeramicItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CeramicItem>)

    @Query("DELETE FROM ceramic_items")
    suspend fun deleteAll()
}