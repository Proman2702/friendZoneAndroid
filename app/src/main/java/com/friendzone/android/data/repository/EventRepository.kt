package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.EventDto
import kotlinx.coroutines.flow.first

class EventRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun getEvents(after: String? = null): List<EventDto> {
        if (prefs.accessToken.first().isNullOrBlank()) return emptyList()
        return runCatching { api.getEvents(after) }
            .getOrElse { throw it.toReadableException() }
    }
}

