package com.example.fotobitacora.utils

import android.content.Context

/** Solo conserva la sesión local (ID), nunca la contraseña. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    val userId: Long get() = prefs.getLong("user_id", -1L)

    fun login(id: Long) {
        prefs.edit().putLong("user_id", id).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
