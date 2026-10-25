package com.example.fotobitacora.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlin.math.abs

/** Captura coordenadas sin usar Google Play Services ni conexión de red obligatoria. */
class GpsLocator(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // Se comprueba el permiso antes de acceder.
    fun locate(callback: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val providers = mutableListOf<String>()
        if (fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            providers.add(LocationManager.GPS_PROVIDER)
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            providers.add(LocationManager.NETWORK_PROVIDER)

        if (providers.isEmpty()) {
            callback(null)
            return
        }

        val now = System.currentTimeMillis()
        val cached = providers.mapNotNull { provider ->
            try { manager.getLastKnownLocation(provider) }
            catch (_: SecurityException) { null }
        }.filter { abs(now - it.time) <= 60_000L }
            .minByOrNull { it.accuracy }

        // Una posición muy reciente evita esperar un nuevo fix innecesariamente.
        if (cached != null && cached.accuracy <= 100f) {
            callback(cached)
            return
        }

        val mainHandler = Handler(Looper.getMainLooper())
        val listeners = mutableListOf<LocationListener>()
        var finished = false
        var timeout: Runnable? = null

        fun finish(location: Location?) {
            if (finished) return
            finished = true
            listeners.forEach { listener ->
                try { manager.removeUpdates(listener) }
                catch (_: SecurityException) { /* permiso revocado durante la consulta */ }
            }
            timeout?.let { mainHandler.removeCallbacks(it) }
            callback(location)
        }

        timeout = Runnable { finish(cached) }
        for (provider in providers) {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) = finish(location)
            }
            try {
                listeners.add(listener)
                manager.requestLocationUpdates(
                    provider, 0L, 0f, listener, Looper.getMainLooper()
                )
            } catch (_: SecurityException) {
                listeners.remove(listener)
            } catch (_: IllegalArgumentException) {
                listeners.remove(listener)
            }
        }
        if (listeners.isEmpty()) finish(cached)
        else mainHandler.postDelayed(timeout, 12_000L)
    }
}
