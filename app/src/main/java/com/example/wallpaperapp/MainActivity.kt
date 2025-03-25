package com.example.wallpaperapp


import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpaperapp.adapters.PhotoAdapter
import com.example.wallpaperapp.models.UnsplashPhoto
import com.example.wallpaperapp.viewmodels.PhotosViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PhotosViewModel
    private lateinit var adapter: PhotoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchLayout: TextInputLayout
    private lateinit var searchEditText: TextInputEditText

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        searchLayout = findViewById(R.id.searchLayout)
        searchEditText = findViewById(R.id.searchEditText)

        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()

        viewModel = ViewModelProvider(this)[PhotosViewModel::class.java]

        viewModel.photos.observe(this) { photos ->
            adapter.setPhotos(photos, true)
            isLoading = false
        }

        viewModel.isLoading.observe(this) { loading ->
            swipeRefreshLayout.isRefreshing = loading && viewModel.photos.value.isNullOrEmpty()
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = PhotoAdapter(this) { photo ->
            val intent = Intent(this, PhotoDetailActivity::class.java).apply {
                putExtra("photoUrl", photo.urls.regular)
                putExtra("photoId", photo.id)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && dy > 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    isLoading = true
                    viewModel.loadPhotos(false)
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                viewModel.loadPhotos(true)
            } else {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "Scroll to the top to refresh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        searchLayout.setEndIconOnClickListener {
            performSearch()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isEmpty()) {
            viewModel.resetSearch()
        } else {
            viewModel.searchPhotos(query)
        }
    }
}