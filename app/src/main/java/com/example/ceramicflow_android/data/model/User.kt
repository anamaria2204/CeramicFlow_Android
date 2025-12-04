package com.example.ceramicflow_android.data.model

data class User(
    val id: String,
    val username: String,
    val email: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)
