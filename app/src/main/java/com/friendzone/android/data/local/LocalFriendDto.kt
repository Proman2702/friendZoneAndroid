package com.friendzone.android.data.local

data class LocalFriendDto(
    val id: String,
    val tag: String,
    val displayName: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Double? = null,
    val locationUpdatedAtIso: String? = null
)
