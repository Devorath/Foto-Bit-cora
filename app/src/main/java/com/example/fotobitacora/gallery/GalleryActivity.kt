package com.example.fotobitacora.gallery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fotobitacora.R
import com.example.fotobitacora.utils.SessionManager
import com.example.fotobitacora.utils.UiUtils
import com.example.fotobitacora.data.DatabaseHelper

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        UiUtils.setInsets(this, findViewById(R.id.rootLayout))
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val userId = SessionManager(this).userId
        if (userId <= 0L) { finish(); return }
        val photos = DatabaseHelper(this).use { it.getPhotosForUser(userId) }
        findViewById<TextView>(R.id.txtEmpty).visibility =
            if (photos.isEmpty()) View.VISIBLE else View.GONE
        findViewById<RecyclerView>(R.id.recyclerPhotos).apply {
            layoutManager = LinearLayoutManager(this@GalleryActivity)
            adapter = PhotoAdapter(photos) { photo ->
                startActivity(Intent(this@GalleryActivity, PhotoDetailActivity::class.java).apply {
                    putExtra(PhotoDetailActivity.EXTRA_PHOTO_ID, photo.id)
                })
            }
        }
    }
}
