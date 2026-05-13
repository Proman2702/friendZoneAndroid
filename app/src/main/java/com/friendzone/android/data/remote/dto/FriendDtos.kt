package com.friendzone.android.data.remote.dto

data class CreateFriendRequest(
    val login: String
)

data class FriendRequestDto(
    val id: String,
    val requester: RemoteUserDto,
    val addressee: RemoteUserDto,
    val status: String,
    val createdAt: String,
    val respondedAt: String? = null
)
