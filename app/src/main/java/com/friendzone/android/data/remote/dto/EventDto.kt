package com.friendzone.android.data.remote.dto

data class EventDto(
    val type: String,
    val zoneId: String,
    val zoneName: String,
    val eventTime: String? = null
)


