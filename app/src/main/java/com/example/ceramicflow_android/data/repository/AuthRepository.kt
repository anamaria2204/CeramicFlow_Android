package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.api.ApiService
import com.example.ceramicflow_android.data.api.RetrofitClient
import com.example.ceramicflow_android.data.datastore.UserPreferencesRepository
import com.example.ceramicflow_android.data.db.UserDao
import com.example.ceramicflow_android.data.model.LoginRequest
import com.example.ceramicflow_android.data.model.LoginResponse
import com.example.ceramicflow_android.data.model.User
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiService: ApiService
) {

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val serverResponse = response.body()!!

                RetrofitClient.setAuthToken(serverResponse.token)

                userPreferencesRepository.saveAuthToken(serverResponse.token)
                userPreferencesRepository.saveUserId(serverResponse.user.id)
                userDao.insertUser(serverResponse.user)
                Result.success(serverResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection error: ${e.message}"))
        }
    }

    suspend fun register(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                // After a successful registration, we immediately log the user in.
                login(request)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection error: ${e.message}"))
        }
    }

    suspend fun logout() {
        userPreferencesRepository.clear()
        userDao.deleteAll()
    }

    suspend fun getLoggedInUser(): User? {
        val userId = userPreferencesRepository.userId.first()
        return if (userId != null) {
            userDao.getUser(userId).first()
        } else {
            null
        }
    }

    val authToken = userPreferencesRepository.authToken
}
