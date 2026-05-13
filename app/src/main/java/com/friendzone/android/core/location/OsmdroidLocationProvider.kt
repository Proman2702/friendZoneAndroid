package com.friendzone.android.core.location

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant

class OsmdroidLocationProvider(private val context: Context) : LocationProvider {
    @SuppressLint("MissingPermission")
    override fun locationUpdates(intervalMillis: Long): Flow<LocationSample> = callbackFlow {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val normalizedInterval = intervalMillis.coerceAtLeast(5_000L)
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            close(SecurityException("Location permission is not granted"))
            return@callbackFlow
        }

        val listener = android.location.LocationListener { location: Location ->
            trySend(location.toSample())
        }

        runCatching {
            manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { trySend(it.toSample()) }
        }
        runCatching {
            manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { trySend(it.toSample()) }
        }

        val looper = Looper.getMainLooper()
        val subscribedProviders = mutableListOf<String>()

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            runCatching {
                manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    normalizedInterval,
                    5f,
                    listener,
                    looper
                )
                subscribedProviders += LocationManager.GPS_PROVIDER
            }
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            runCatching {
                manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    normalizedInterval.coerceAtLeast(10_000L),
                    10f,
                    listener,
                    looper
                )
                subscribedProviders += LocationManager.NETWORK_PROVIDER
            }
        }

        if (subscribedProviders.isEmpty()) {
            close(IllegalStateException("No enabled location providers"))
            return@callbackFlow
        }

        awaitClose {
            manager.removeUpdates(listener)
        }
    }

    private fun Location.toSample(): LocationSample =
        LocationSample(
            lat = latitude,
            lon = longitude,
            accuracy = accuracy.toDouble(),
            deviceTimeIso = Instant.ofEpochMilli(time).toString()
        )
}


