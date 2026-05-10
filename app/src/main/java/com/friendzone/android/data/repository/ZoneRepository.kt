package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.ZoneDto
import java.util.UUID

class ZoneRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun listZones(): List<ZoneDto> {
        // Временно работаем только с локальным списком зон.
        return prefs.getLocalZones()

        // Возврат к серверу:
        // val clientId = requireClientId()
        // return api.getZones(clientId)
    }

    suspend fun createZone(
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean
    ): ZoneDto {
        val zones = prefs.getLocalZones()
        val zone = ZoneDto(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            centerLat = centerLat,
            centerLon = centerLon,
            radiusMeters = radiusMeters,
            isActive = isActive
        )
        prefs.saveLocalZones(zones + zone)
        return zone

        // Возврат к серверу:
        // val clientId = requireClientId()
        // return api.createZone(...)
    }

    suspend fun updateZone(
        zoneId: String,
        name: String,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        isActive: Boolean
    ): ZoneDto {
        val updatedZone = ZoneDto(
            id = zoneId,
            name = name.trim(),
            centerLat = centerLat,
            centerLon = centerLon,
            radiusMeters = radiusMeters,
            isActive = isActive
        )
        val updatedZones = prefs.getLocalZones().map { existing ->
            if (existing.id == zoneId) updatedZone else existing
        }
        prefs.saveLocalZones(updatedZones)
        return updatedZone

        // Возврат к серверу:
        // val clientId = requireClientId()
        // return api.updateZone(...)
    }

    suspend fun deleteZone(zoneId: String) {
        val updatedZones = prefs.getLocalZones().filterNot { it.id == zoneId }
        prefs.saveLocalZones(updatedZones)

        // Возврат к серверу:
        // val clientId = requireClientId()
        // api.deleteZone(zoneId, clientId)
    }

    suspend fun getZone(zoneId: String): ZoneDto? {
        return prefs.getLocalZones().firstOrNull { it.id == zoneId }
    }
}
