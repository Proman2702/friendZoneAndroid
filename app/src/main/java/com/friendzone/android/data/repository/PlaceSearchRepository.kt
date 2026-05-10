package com.friendzone.android.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class PlaceSearchResult(
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@Singleton
class PlaceSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun search(query: String): PlaceSearchResult? {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return null
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context, Locale("ru"))
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // На новых Android Geocoder работает через callback, поэтому оборачиваем его в suspend.
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(trimmed, 1) { results ->
                    continuation.resume(results.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(trimmed, 1)?.firstOrNull()
        } ?: return null

        return PlaceSearchResult(
            title = address.featureName ?: trimmed,
            address = address.getAddressLine(0) ?: trimmed,
            latitude = address.latitude,
            longitude = address.longitude
        )
    }
}
