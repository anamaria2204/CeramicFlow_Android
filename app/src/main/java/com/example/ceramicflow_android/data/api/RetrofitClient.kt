package com.example.ceramicflow_android.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // 1. Variabilă care va ține minte token-ul după ce te loghezi
    private var authToken: String? = null

    // 2. Funcție ca să setăm token-ul din LoginActivity (sau unde faci login)
    fun setAuthToken(token: String) {
        authToken = token
    }

    // 3. Configurăm clientul HTTP (OkHttp) cu un Interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            if (!authToken.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // <--- 4. Spunem lui Retrofit să folosească clientul nostru modificat
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}