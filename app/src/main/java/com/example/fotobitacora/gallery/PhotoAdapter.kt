package com.example.fotobitacora.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fotobitacora.model.Photo
import com.example.fotobitacora.R
import com.example.fotobitacora.utils.UiUtils

class PhotoAdapter(
    private val photos: List<Photo>,
    private val onTap: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>() {

    class PhotoHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
        val comment: TextView = view.findViewById(R.id.txtComment)
        val date: TextView = view.findViewById(R.id.txtDate)
        val gps: TextView = view.findViewById(R.id.txtGps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoHolder(item)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val photo = photos[position]
        holder.comment.text = photo.comment
        holder.date.text = UiUtils.date(photo.createdAt)
        holder.gps.text = UiUtils.coordinates(photo.latitude, photo.longitude)
        UiUtils.loadPhoto(holder.thumbnail, photo.filePath, 250)
        holder.itemView.setOnClickListener { onTap(photo) }
    }

    override fun getItemCount(): Int = photos.size
}
