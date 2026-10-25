package com.example.fotobitacora.ui.main

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.fotobitacora.R
import com.example.fotobitacora.data.DatabaseHelper
import com.example.fotobitacora.data.PhotoStorage
import com.example.fotobitacora.gallery.GalleryActivity
import com.example.fotobitacora.ui.login.LoginActivity
import com.example.fotobitacora.utils.GpsLocator
import com.example.fotobitacora.utils.SessionManager
import com.example.fotobitacora.utils.UiUtils
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var gps: GpsLocator
    private lateinit var preview: ImageView
    private lateinit var emptyPreview: TextView
    private lateinit var takeButton: Button

    private var pendingFile: File? = null
    private val permissionLauncher = registerForActivityResult(

        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (gps.hasLocationPermission()) startCamera()
        else Toast.makeText(this, "Permite ubicación para registrar la foto", Toast.LENGTH_LONG).show()
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingFile?.exists() == true && (pendingFile?.length() ?: 0L) > 0L) {
            displayPhoto(pendingFile!!.absolutePath)
            lookupLocation()
        } else {
            discardPending()
            Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        session = SessionManager(this)
        gps = GpsLocator(this)
        if (session.userId <= 0L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        UiUtils.setInsets(this, findViewById(R.id.rootLayout))

        pendingFile = savedInstanceState?.getString(KEY_PENDING)?.let { File(it) }
        preview = findViewById(R.id.imgPreview)
        emptyPreview = findViewById(R.id.txtEmptyPreview)
        takeButton = findViewById(R.id.btnTakePhoto)
        takeButton.isEnabled = pendingFile == null
        findViewById<TextView>(R.id.txtWelcome).text =
            "Hola, ${db.getFullName(session.userId) ?: "usuario"}"

        takeButton.setOnClickListener {
            if (gps.hasLocationPermission()) startCamera()
            else permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
        findViewById<Button>(R.id.btnViewPhotos).setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logout()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
        showLastSavedPhoto()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_PENDING, pendingFile?.absolutePath)
        super.onSaveInstanceState(outState)
    }

    private fun startCamera() {
        try {
            val file = PhotoStorage.createPhotoFile(this)
            pendingFile = file
            takeButton.isEnabled = false
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            discardPending()
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_LONG).show()
        }
    }

    private fun lookupLocation() {
        Toast.makeText(this, "Obteniendo ubicación del dispositivo…", Toast.LENGTH_SHORT).show()
        gps.locate { location ->
            if (isFinishing || isDestroyed) return@locate
            if (location == null) {
                AlertDialog.Builder(this)
                    .setTitle("No se obtuvo ubicación")
                    .setMessage("Activa la ubicación y permite el acceso. Es necesaria para guardar la foto.")
                    .setPositiveButton("Reintentar") { _, _ -> lookupLocation() }
                    .setNegativeButton("Descartar foto") { _, _ -> discardPending() }
                    .setCancelable(false)
                    .show()
            } else {
                requestComment(location)
            }
        }
    }

    private fun requestComment(location: Location) {
        val dp = resources.displayMetrics.density
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * dp).toInt()
            setPadding(pad, (6 * dp).toInt(), pad, 0)
        }
        val coords = TextView(this).apply {
            text = "Ubicación: ${UiUtils.coordinates(location.latitude, location.longitude)}"
        }
        val edit = EditText(this).apply {
            hint = "Describe la fotografía"
            minLines = 2
            maxLines = 4
            filters = arrayOf(InputFilter.LengthFilter(500))
        }
        content.addView(coords)
        content.addView(edit)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Registrar fotografía")
            .setView(content)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Descartar") { _, _ -> discardPending() }
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val comment = edit.text.toString().trim()
                if (comment.isEmpty()) {
                    edit.error = "Escribe un comentario"
                    return@setOnClickListener
                }
                val path = pendingFile?.absolutePath ?: return@setOnClickListener
                try {
                    db.savePhoto(
                        session.userId, path, location.latitude, location.longitude,
                        location.accuracy, comment
                    )
                    pendingFile = null
                    takeButton.isEnabled = true
                    dialog.dismiss()
                    Toast.makeText(this, "Foto, GPS y comentario guardados", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(this, "Error al guardar. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }

    private fun displayPhoto(path: String) {
        emptyPreview.visibility = View.GONE
        preview.visibility = View.VISIBLE
        UiUtils.loadPhoto(preview, path)
    }

    private fun showLastSavedPhoto() {
        if (pendingFile != null) {
            pendingFile?.absolutePath?.let { displayPhoto(it) }
            return
        }
        val last = db.getPhotosForUser(session.userId).firstOrNull()
        if (last == null) {
            preview.visibility = View.GONE
            emptyPreview.visibility = View.VISIBLE
        } else displayPhoto(last.filePath)
    }

    private fun discardPending() {
        pendingFile?.delete()
        pendingFile = null
        takeButton.isEnabled = true
        showLastSavedPhoto()
    }

    companion object {
        private const val KEY_PENDING = "pending_photo_path"
    }
}
