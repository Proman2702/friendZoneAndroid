package com.friendzone.android.data.remote.dto

data class LocationRequest(
    val lat: Double,
    val lon: Double,
    val accuracy: Double,
    val deviceTime: String
)

