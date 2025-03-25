package com.example.wallpaperapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wallpaperapp.R
import com.example.wallpaperapp.models.UnsplashPhoto
import kotlin.random.Random

class PhotoAdapter(
    private val context: Context,
    private val onPhotoClick: (UnsplashPhoto) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private val photos = mutableListOf<UnsplashPhoto>()
    private val minHeight = 300
    private val maxHeight = 600

    fun setPhotos(newPhotos: List<UnsplashPhoto>, refresh: Boolean = false) {
        if (refresh) {
            photos.clear()
        }
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        val aspectRatio = photo.width.toFloat() / photo.height.toFloat()
        val randomHeight = Random.nextInt(minHeight, maxHeight)
        val calculatedHeight = if (aspectRatio > 1) {
            (randomHeight * 0.8).toInt()
        } else {
            (randomHeight * 1.2).toInt()
        }

        val params = holder.imageView.layoutParams
        params.height = calculatedHeight
        holder.imageView.layoutParams = params

        Glide.with(context)
            .load(photo.urls.regular)
            .thumbnail(Glide.with(context).load(photo.urls.thumb))
            .centerCrop()
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onPhotoClick(photo)
        }
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}