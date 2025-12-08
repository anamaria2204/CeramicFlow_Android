package com.example.ceramicflow_android.data.repository

import com.example.ceramicflow_android.data.model.LoginRequest
import com.example.ceramicflow_android.data.model.LoginResponse
import com.example.ceramicflow_android.data.model.User
import kotlinx.coroutines.delay

class AuthRepository {
    // Mock users database
    private val mockUsers = mapOf(
        "admin" to "admin123",
        "user" to "user123",
        "maria" to "maria123"
    )

    // Simulate JWT token generation
    private fun generateMockJWT(username: String): String {
        return "mock.jwt.token.${username}.${System.currentTimeMillis()}"
    }

    // Simulate login with network delay
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        delay(800) // Simulate network delay

        val password = mockUsers[request.username]
        
        return if (password != null && password == request.password) {
            val user = User(
                id = "user_${System.currentTimeMillis()}",
                username = request.username,
                email = "${request.username}@ceramicflow.com"
            )
            val token = generateMockJWT(request.username)
            Result.success(LoginResponse(token = token, user = user))
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    // Validate mock JWT token
    suspend fun validateToken(token: String): Boolean {
        delay(200)
        return token.startsWith("mock.jwt.token")
    }

    // Store token (in real app, use SharedPreferences or DataStore)
    private var currentToken: String? = null
    
    fun saveToken(token: String) {
        currentToken = token
    }

    fun getToken(): String? {
        return currentToken
    }

    fun clearToken() {
        currentToken = null
    }

    fun isLoggedIn(): Boolean {
        return currentToken != null
    }
}
