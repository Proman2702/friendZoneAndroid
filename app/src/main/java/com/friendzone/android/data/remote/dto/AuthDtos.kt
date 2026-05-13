package com.friendzone.android.data.remote.dto

data class AuthRegisterRequest(
    val login: String,
    val password: String,
    val displayName: String? = null
)

data class AuthLoginRequest(
    val login: String,
    val password: String
)

data class RemoteUserDto(
    val id: String,
    val login: String,
    val displayName: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: RemoteUserDto
)


