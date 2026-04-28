package com.friendzone.android.core.location

import kotlinx.coroutines.flow.Flow

data class LocationSample(
    val lat: Double,
    val lon: Double,
    val accuracy: Double,
    val deviceTimeIso: String
)

interface LocationProvider {
    fun locationUpdates(): Flow<LocationSample>
}


