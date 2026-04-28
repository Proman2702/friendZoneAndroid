package com.friendzone.android.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant

class OsmdroidLocationProvider(private val context: Context) : LocationProvider {
    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<LocationSample> = callbackFlow {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = android.location.LocationListener { location: Location ->
            trySend(location.toSample())
        }

        val looper = Looper.getMainLooper()
        manager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5_000L,
            5f,
            listener,
            looper
        )
        manager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            10_000L,
            10f,
            listener,
            looper
        )

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


