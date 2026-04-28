package com.friendzone.android.data.remote.dto

data class LocationBatchRequest(
    val clientId: String,
    val samples: List<LocationSampleDto>
)


