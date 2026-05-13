package com.friendzone.android.data.remote.dto

data class EventDto(
    val type: String,
    val zoneId: String,
    val zoneName: String,
    val recipientClientId: String? = null,
    val actorClientId: String? = null,
    val actorLogin: String? = null,
    val eventTime: String? = null
)

