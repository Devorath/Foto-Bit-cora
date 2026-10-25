package com.example.fotobitacora.utils

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UiUtils {
    /** Evita que Android 15 dibuje los controles debajo de barras del sistema. */
    fun setInsets(activity: AppCompatActivity, root: View) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val l = root.paddingLeft
        val t = root.paddingTop
        val r = root.paddingRight
        val b = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(l + bars.left, t + bars.top, r + bars.right, b + bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    /** Decodifica una miniatura; evita abrir en RAM una foto a resolución completa. */
    fun loadPhoto(image: ImageView, filePath: String, maxPixels: Int = 1100) {
        if (!File(filePath).exists()) {
            image.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(filePath, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxPixels || bounds.outHeight / sample > maxPixels) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        if (bitmap != null) image.setImageBitmap(bitmap)
        else image.setImageResource(android.R.drawable.ic_menu_gallery)
    }

    fun date(millis: Long): String = SimpleDateFormat(
        "dd/MM/yyyy HH:mm", Locale.getDefault()
    ).format(Date(millis))

    fun coordinates(lat: Double, lon: Double): String =
        String.format(Locale.US, "%.6f, %.6f", lat, lon)
}
