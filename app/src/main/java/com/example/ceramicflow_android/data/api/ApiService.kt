package com.example.ceramicflow_android.data.api

import com.example.ceramicflow_android.data.model.CeramicItem
import com.example.ceramicflow_android.data.model.LoginRequest
import com.example.ceramicflow_android.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("ceramics")
    suspend fun getCeramics(): List<CeramicItem>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: LoginRequest): Response<LoginResponse>
}