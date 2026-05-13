package com.friendzone.android.data.local

data class LocalLocationDto(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val deviceTimeIso: String
)
