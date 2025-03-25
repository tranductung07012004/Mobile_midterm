package com.example.wallpaperapp

import android.Manifest
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton
import com.otaliastudios.zoom.ZoomLayout
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var zoomLayout: ZoomLayout
    private lateinit var imageView: ImageView
    private lateinit var backButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var fullscreenButton: MaterialButton
    private lateinit var exitFullScreenButton: MaterialButton
    private lateinit var setWallPaper: MaterialButton
    private lateinit var buttonPanel: LinearLayout

    private var photoUrl: String? = null
    private var photoId: String? = null
    private var isFullscreen = false

    companion object {
        private const val PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        photoUrl = intent.getStringExtra("photoUrl")
        photoId = intent.getStringExtra("photoId")

        zoomLayout = findViewById(R.id.zoomLayout)
        imageView = findViewById(R.id.detailImageView)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        shareButton = findViewById(R.id.shareButton)
        fullscreenButton = findViewById(R.id.fullscreenButton)
        setWallPaper = findViewById(R.id.wallpaper)
        buttonPanel = findViewById(R.id.buttonPanel)
        exitFullScreenButton = findViewById(R.id.exitFullscreenButton)


        zoomLayout.setZoomEnabled(false)
        zoomLayout.setVerticalPanEnabled(false)
        zoomLayout.setHorizontalPanEnabled(false)

        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.error_place_holder)
            .thumbnail(0.1f)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    zoomLayout.post {
                        imageView.setImageDrawable(resource)
                        zoomLayout.zoomTo(1.0f, true)
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        setupButtons()
    }

    private fun setupButtons() {
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            if (checkPermission()) {
                saveImageToGallery()
            } else {
                requestPermission()
            }
        }

        shareButton.setOnClickListener { shareImage() }

        fullscreenButton.setOnClickListener { toggleFullscreen() }

        exitFullScreenButton.setOnClickListener { toggleFullscreen() }

        setWallPaper.setOnClickListener { setWallpaper() }
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen

        if (isFullscreen) {
            zoomLayout.setZoomEnabled(true)
            zoomLayout.setVerticalPanEnabled(true)
            zoomLayout.setHorizontalPanEnabled(true)
            buttonPanel.visibility = View.GONE
            exitFullScreenButton.visibility = View.VISIBLE
        } else {
            zoomLayout.zoomTo(1.0f, true)
            zoomLayout.setZoomEnabled(false)
            zoomLayout.setVerticalPanEnabled(false)
            zoomLayout.setHorizontalPanEnabled(false)
            buttonPanel.visibility = View.VISIBLE
            exitFullScreenButton.visibility = View.GONE
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery()
            } else {
                Toast.makeText(
                    this,
                    "Storage permission is required to save images",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveImageToGallery() {
        Glide.with(this)
            .asBitmap()
            .load(photoUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val saved = saveImage(resource)
                    if (saved) {
                        Toast.makeText(
                            this@PhotoDetailActivity,
                            "Image saved to gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@PhotoDetailActivity,
                            "Failed to save image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun saveImage(bitmap: Bitmap): Boolean {
        val filename = "Wallpaper_${photoId ?: System.currentTimeMillis()}.jpg"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return false

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                true
            }
            else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!imagesDir.exists()) {
                    imagesDir.mkdir()
                }

                val image = File(imagesDir, filename)
                val outputStream: OutputStream = FileOutputStream(image)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                outputStream.flush()
                outputStream.close()

                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    image.absolutePath,
                    filename,
                    "Wallpaper image"
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun shareImage() {
        Glide.with(this)
            .asBitmap()
            .load(photoUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val cachePath = File(cacheDir, "images")
                    cachePath.mkdirs()
                    val file = File(cachePath, "shared_image.jpg")

                    try {
                        val fileOutputStream = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                        fileOutputStream.flush()
                        fileOutputStream.close()

                        val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            androidx.core.content.FileProvider.getUriForFile(
                                this@PhotoDetailActivity,
                                "${packageName}.provider",
                                file
                            )
                        } else {
                            Uri.fromFile(file)
                        }

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        startActivity(Intent.createChooser(intent, "Share image via"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@PhotoDetailActivity,
                            "Error sharing image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen()
        } else {
            super.onBackPressed()
        }
    }

    private fun setWallpaper() {
        Glide.with(this)
            .asBitmap()
            .load(photoUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                        wallpaperManager.setBitmap(resource)
                        Toast.makeText(this@PhotoDetailActivity, "Already added as wall paper", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@PhotoDetailActivity, "Failed to add as wall paper", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

}