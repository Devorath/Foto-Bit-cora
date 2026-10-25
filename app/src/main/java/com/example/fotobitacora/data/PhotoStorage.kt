package com.example.fotobitacora.data

import android.content.Context
import android.os.Environment
import java.io.File

object PhotoStorage {
    /** Pictures de la propia app; no requiere permisos de almacenamiento. */
    fun createPhotoFile(context: Context): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "Pictures")
        check(dir.exists() || dir.mkdirs()) { "No se pudo crear la carpeta de fotos" }
        return File.createTempFile("FOTO_${System.currentTimeMillis()}_", ".jpg", dir)
    }
}
