package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.EventDto
import com.friendzone.android.data.remote.dto.LocationBatchRequest
import com.friendzone.android.data.remote.dto.LocationSampleDto
import kotlinx.coroutines.flow.first

class LocationRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun sendBatch(samples: List<LocationSampleDto>): List<EventDto> {
        val clientId = prefs.clientId.first() ?: error("clientId is missing. Registration not completed.")
        val response = api.sendLocations(LocationBatchRequest(clientId, samples))
        return response.events
    }
}


