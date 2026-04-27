package com.friendzone.android.network.dto

data class LocationRequest(
    val clientId: String,
    val lat: Double,
    val lon: Double,
    val accuracy: Double,
    val deviceTime: String
)
