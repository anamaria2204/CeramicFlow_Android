package com.example.ceramicflow_android.data.api

import com.example.ceramicflow_android.data.model.LoginRequest
import com.example.ceramicflow_android.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}