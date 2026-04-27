package com.friendzone.android.network.dto

data class ZoneDto(
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean
)
