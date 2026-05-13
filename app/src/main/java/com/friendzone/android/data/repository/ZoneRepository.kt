package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.ZoneCreateRequest
import com.friendzone.android.data.remote.dto.ZoneDto
import com.friendzone.android.data.remote.dto.ZoneUpdateRequest
import kotlinx.coroutines.flow.first

class ZoneRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun listZones(): List<ZoneDto> {
        ensureLoggedIn()
        return runCatching { api.getZones() }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun createZone(
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean,
        detectorFriendIds: List<String> = emptyList()
    ): ZoneDto {
        ensureLoggedIn()
        return runCatching {
            api.createZone(
                ZoneCreateRequest(
                    name = name.trim(),
                    centerLat = centerLat,
                    centerLon = centerLon,
                    radiusMeters = radiusMeters,
                    isActive = isActive,
                    notifyFriendIds = detectorFriendIds
                )
            )
        }.getOrElse { throw it.toReadableException() }
    }

    suspend fun updateZone(
        zoneId: String,
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean,
        detectorFriendIds: List<String> = emptyList()
    ): ZoneDto {
        ensureLoggedIn()
        return runCatching {
            api.updateZone(
                zoneId = zoneId,
                request = ZoneUpdateRequest(
                    name = name.trim(),
                    centerLat = centerLat,
                    centerLon = centerLon,
                    radiusMeters = radiusMeters,
                    isActive = isActive,
                    notifyFriendIds = detectorFriendIds
                )
            )
        }.getOrElse { throw it.toReadableException() }
    }

    suspend fun deleteZone(zoneId: String) {
        ensureLoggedIn()
        runCatching { api.deleteZone(zoneId) }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun getZone(zoneId: String): ZoneDto? {
        return listZones().firstOrNull { it.id == zoneId }
    }

    private suspend fun ensureLoggedIn() {
        if (prefs.accessToken.first().isNullOrBlank()) {
            error("Login required")
        }
    }
}
