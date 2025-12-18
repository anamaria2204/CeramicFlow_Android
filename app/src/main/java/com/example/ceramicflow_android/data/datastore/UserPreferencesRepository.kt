package com.example.ceramicflow_android.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private val authTokenKey = stringPreferencesKey("auth_token")
    private val userIdKey = stringPreferencesKey("user_id")

    val authToken: Flow<String?> = context.dataStore.data
        .map {
            it[authTokenKey]
        }

    val userId: Flow<String?> = context.dataStore.data
        .map {
            it[userIdKey]
        }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit {
            it[authTokenKey] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit {
            it[userIdKey] = userId
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.clear()
        }
    }
}