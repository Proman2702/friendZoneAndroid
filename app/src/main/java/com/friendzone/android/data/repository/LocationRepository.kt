package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.local.LocalLocationDto
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.EventDto
import com.friendzone.android.data.remote.dto.LocationBatchRequest
import com.friendzone.android.data.remote.dto.LocationRequest
import com.friendzone.android.data.remote.dto.LocationSampleDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LocationRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    val currentDeviceLocation: Flow<LocalLocationDto?> = prefs.currentDeviceLocation

    suspend fun saveDeviceLocation(sample: LocationSampleDto) {
        prefs.saveCurrentDeviceLocation(
            LocalLocationDto(
                latitude = sample.lat,
                longitude = sample.lon,
                accuracy = sample.accuracy,
                deviceTimeIso = sample.deviceTime
            )
        )
    }

    suspend fun send(sample: LocationSampleDto): List<EventDto> {
        saveDeviceLocation(sample)
        if (prefs.accessToken.first().isNullOrBlank()) return emptyList()
        val response = runCatching {
            api.sendLocation(
                LocationRequest(
                    lat = sample.lat,
                    lon = sample.lon,
                    accuracy = sample.accuracy,
                    deviceTime = sample.deviceTime
                )
            )
        }.getOrElse { throw it.toReadableException() }
        return response.events
    }

    suspend fun sendBatch(samples: List<LocationSampleDto>): List<EventDto> {
        samples.lastOrNull()?.let { saveDeviceLocation(it) }
        if (prefs.accessToken.first().isNullOrBlank()) return emptyList()
        val response = runCatching {
            api.sendLocations(LocationBatchRequest(samples))
        }.getOrElse { throw it.toReadableException() }
        return response.events
    }
}

