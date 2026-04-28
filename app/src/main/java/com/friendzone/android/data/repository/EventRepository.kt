package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.EventDto
import kotlinx.coroutines.flow.first

class EventRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun getEvents(): List<EventDto> {
        val clientId = prefs.clientId.first() ?: error("clientId is missing. Registration not completed.")
        return api.getEvents(clientId)
    }
}


