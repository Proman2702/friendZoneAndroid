package com.friendzone.android.data.remote.dto

data class ZoneUpdateRequest(
    val clientId: String,
    val name: String? = null,
    val centerLat: Double? = null,
    val centerLon: Double? = null,
    val radiusMeters: Double? = null,
    val isActive: Boolean? = null
)


