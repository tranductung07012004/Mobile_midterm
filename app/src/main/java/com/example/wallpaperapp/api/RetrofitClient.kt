package com.example.wallpaperapp.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.wallpaperapp.BuildConfig


object RetrofitClient {
    private const val BASE_URL = "https://api.unsplash.com/"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val url = originalRequest.url.newBuilder()
                .addQueryParameter("client_id", BuildConfig.unsplashApiKey)
                .build()
            val newRequest = originalRequest.newBuilder().url(url).build()
            chain.proceed(newRequest)
        }
        .addInterceptor(logger)
        .build()


    val unsplashApi: UnsplashApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApiService::class.java)
    }
}