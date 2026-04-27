package com.friendzone.android.data

import com.friendzone.android.network.FriendZoneApi
import com.friendzone.android.network.dto.ZoneCreateRequest
import com.friendzone.android.network.dto.ZoneDto
import com.friendzone.android.network.dto.ZoneUpdateRequest
import kotlinx.coroutines.flow.first

class ZoneRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun listZones(): List<ZoneDto> {
        val clientId = requireClientId()
        return api.getZones(clientId)
    }

    suspend fun createZone(
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean
    ): ZoneDto {
        val clientId = requireClientId()
        return api.createZone(
            ZoneCreateRequest(
                clientId = clientId,
                name = name,
                centerLat = centerLat,
                centerLon = centerLon,
                radiusMeters = radiusMeters,
                isActive = isActive
            )
        )
    }

    suspend fun updateZone(
        zoneId: String,
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean
    ): ZoneDto {
        val clientId = requireClientId()
        return api.updateZone(
            zoneId,
            ZoneUpdateRequest(
                clientId = clientId,
                name = name,
                centerLat = centerLat,
                centerLon = centerLon,
                radiusMeters = radiusMeters,
                isActive = isActive
            )
        )
    }

    suspend fun deleteZone(zoneId: String) {
        val clientId = requireClientId()
        api.deleteZone(zoneId, clientId)
    }

    private suspend fun requireClientId(): String =
        prefs.clientId.first() ?: error("clientId is missing. Registration not completed.")
}
