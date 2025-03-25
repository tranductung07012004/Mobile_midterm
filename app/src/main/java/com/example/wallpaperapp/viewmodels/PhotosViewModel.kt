package com.example.wallpaperapp.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaperapp.models.UnsplashPhoto
import com.example.wallpaperapp.repository.UnsplashRepository
import kotlinx.coroutines.launch

class PhotosViewModel : ViewModel() {
    private val repository = UnsplashRepository()

    private val _photos = MutableLiveData<List<UnsplashPhoto>>()
    val photos: LiveData<List<UnsplashPhoto>> get() = _photos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var currentPage = 1
    private var currentQuery: String? = null
    private var isLastPage = false

    init {
        loadPhotos(refresh = true)
    }

    fun loadPhotos(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            isLastPage = false
        }

        if (isLastPage) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val newPhotos = if (currentQuery.isNullOrBlank()) {
                    repository.getPhotos(currentPage)
                } else {
                    repository.searchPhotos(currentQuery!!, currentPage)
                }

                if (newPhotos.isEmpty()) {
                    isLastPage = true
                } else {
                    if (refresh) {
                        _photos.value = newPhotos
                    } else {
                        _photos.value = _photos.value.orEmpty() + newPhotos
                    }
                    currentPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPhotos(query: String) {
        currentQuery = query
        loadPhotos(refresh = true)
    }

    fun resetSearch() {
        currentQuery = null
        loadPhotos(refresh = true)
    }
}