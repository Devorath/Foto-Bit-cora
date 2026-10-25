package com.example.fotobitacora.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fotobitacora.model.Photo
import com.example.fotobitacora.utils.PasswordUtils
import java.util.Locale

/** Base de datos SQLite local. No se guarda la imagen como BLOB, sino su ruta. */
class DatabaseHelper(context: Context) : android.database.sqlite.SQLiteOpenHelper(context,
    DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                password_salt TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                file_path TEXT NOT NULL UNIQUE,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                accuracy_meters REAL NOT NULL,
                comment TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_photos_user_time ON photos(user_id, created_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En versiones futuras incorporar migraciones sin borrar los datos existentes.
        throw IllegalStateException("No existe migración de BD $oldVersion a $newVersion")
    }

    fun createUser(firstName: String, lastName: String, email: String, password: String): Long {
        val salt = PasswordUtils.newSalt()
        val values = ContentValues().apply {
            put("first_name", firstName.trim())
            put("last_name", lastName.trim())
            put("email", email.trim().lowercase(Locale.ROOT))
            put("password_salt", salt)
            put("password_hash", PasswordUtils.hash(password, salt))
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insertOrThrow("users", null, values)
    }

    /** Devuelve el ID del usuario si el correo y la contraseña coinciden. */
    fun login(email: String, password: String): Long? {
        val normalized = email.trim().lowercase(Locale.ROOT)
        readableDatabase.query(
            "users", arrayOf("id", "password_salt", "password_hash"),
            "email = ?", arrayOf(normalized), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val salt = cursor.getString(cursor.getColumnIndexOrThrow("password_salt"))
            val hash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"))
            if (!PasswordUtils.verify(password, salt, hash)) return null
            return cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        }
    }

    fun getFullName(userId: Long): String? {
        readableDatabase.query(
            "users", arrayOf("first_name", "last_name"),
            "id = ?", arrayOf(userId.toString()), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return "${cursor.getString(0)} ${cursor.getString(1)}"
        }
    }

    fun savePhoto(
        userId: Long, path: String, latitude: Double, longitude: Double,
        accuracyMeters: Float, comment: String
    ): Long {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("file_path", path)
            put("latitude", latitude)
            put("longitude", longitude)
            put("accuracy_meters", accuracyMeters)
            put("comment", comment.trim())
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insertOrThrow("photos", null, values)
    }

    fun getPhotosForUser(userId: Long): List<Photo> {
        val results = mutableListOf<Photo>()
        readableDatabase.query(
            "photos", null, "user_id = ?", arrayOf(userId.toString()),
            null, null, "created_at DESC, id DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) results.add(cursor.toPhoto())
        }
        return results
    }

    /** El ID de usuario también se comprueba para evitar leer fotos ajenas. */
    fun getPhotoForUser(photoId: Long, userId: Long): Photo? {
        readableDatabase.query(
            "photos", null, "id = ? AND user_id = ?",
            arrayOf(photoId.toString(), userId.toString()), null, null, null
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toPhoto() else null
        }
    }

    private fun Cursor.toPhoto() = Photo(
        id = getLong(getColumnIndexOrThrow("id")),
        userId = getLong(getColumnIndexOrThrow("user_id")),
        filePath = getString(getColumnIndexOrThrow("file_path")),
        latitude = getDouble(getColumnIndexOrThrow("latitude")),
        longitude = getDouble(getColumnIndexOrThrow("longitude")),
        accuracyMeters = getFloat(getColumnIndexOrThrow("accuracy_meters")),
        comment = getString(getColumnIndexOrThrow("comment")),
        createdAt = getLong(getColumnIndexOrThrow("created_at"))
    )

    companion object {
        private const val DB_NAME = "fotobitacora.db"
        private const val DB_VERSION = 1
    }
}
