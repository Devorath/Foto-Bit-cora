package com.example.fotobitacora.gallery

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fotobitacora.R
import com.example.fotobitacora.utils.SessionManager
import com.example.fotobitacora.utils.UiUtils
import java.util.Locale
import com.example.fotobitacora.data.DatabaseHelper

class PhotoDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)
        UiUtils.setInsets(this, findViewById(R.id.rootLayout))
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        val userId = SessionManager(this).userId
        val photoId = intent.getLongExtra(EXTRA_PHOTO_ID, -1L)
        if (userId <= 0L || photoId <= 0L) { finish(); return }
        val photo = DatabaseHelper(this)
            .use { it.getPhotoForUser(photoId, userId) }
        if (photo == null) {
            Toast.makeText(this, "Fotografía no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        UiUtils.loadPhoto(findViewById<ImageView>(R.id.imgDetail), photo.filePath, 1600)
        findViewById<TextView>(R.id.txtComment).text = photo.comment
        findViewById<TextView>(R.id.txtCoordinates).text =
            "Latitud: ${String.format(Locale.US, "%.6f", photo.latitude)}\n" +
            "Longitud: ${String.format(Locale.US, "%.6f", photo.longitude)}"
        findViewById<TextView>(R.id.txtAccuracy).text =
            "Precisión estimada: ±${photo.accuracyMeters.toInt()} m"
        findViewById<TextView>(R.id.txtDate).text =
            "Capturada el ${UiUtils.date(photo.createdAt)}"
    }

    companion object {
        const val EXTRA_PHOTO_ID = "photo_id"
    }
}
