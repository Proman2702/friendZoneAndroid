package com.friendzone.android.data.remote.dto

data class ZoneCreateRequest(
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean,
    val notifyFriendIds: List<String> = emptyList()
)

