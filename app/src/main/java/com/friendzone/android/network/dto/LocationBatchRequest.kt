package com.friendzone.android.network.dto

data class LocationBatchRequest(
    val clientId: String,
    val samples: List<LocationSampleDto>
)
