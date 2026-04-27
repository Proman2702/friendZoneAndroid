package com.friendzone.android.network.dto

data class ZoneCreateRequest(
    val clientId: String,
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean
)
