package com.friendzone.android.data.remote.dto

data class ZoneDto(
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean,
    val detectorFriendIds: List<String> = emptyList()
)


