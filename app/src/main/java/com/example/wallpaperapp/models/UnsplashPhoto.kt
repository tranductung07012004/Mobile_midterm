package com.example.wallpaperapp.models

data class UnsplashPhoto(
    val id: String,
    val created_at: String,
    val width: Int,
    val height: Int,
    val color: String,
    val description: String?,
    val urls: UnsplashUrls,
    val user: UnsplashUser
)

data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String
)

data class UnsplashSearchResponse(
    val total: Int,
    val total_pages: Int,
    val results: List<UnsplashPhoto>
)