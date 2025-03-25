package com.example.wallpaperapp.repository


import com.example.wallpaperapp.api.RetrofitClient
import com.example.wallpaperapp.models.UnsplashPhoto
import com.example.wallpaperapp.BuildConfig

class UnsplashRepository {
    private val apiService = RetrofitClient.unsplashApi

    suspend fun getPhotos(page: Int): List<UnsplashPhoto> {
        return apiService.getPhotos(BuildConfig.unsplashApiKey, page)
    }

    suspend fun searchPhotos(query: String, page: Int): List<UnsplashPhoto> {
        return apiService.searchPhotos(BuildConfig.unsplashApiKey, query, page).results
    }
}