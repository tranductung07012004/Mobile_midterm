package com.example.wallpaperapp.api

import com.example.wallpaperapp.models.UnsplashPhoto
import com.example.wallpaperapp.models.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("photos")
    suspend fun getPhotos(
        @Query("client_id") clientId: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20
    ): List<UnsplashPhoto>

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("client_id") clientId: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20
    ): UnsplashSearchResponse
}