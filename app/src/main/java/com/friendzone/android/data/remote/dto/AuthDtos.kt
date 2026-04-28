package com.friendzone.android.data.remote.dto

data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthLoginRequest(
    val email: String,
    val password: String
)

data class PasswordRecoveryRequest(
    val email: String
)

data class AuthUserResponse(
    val clientId: String,
    val name: String,
    val email: String
)

data class PasswordRecoveryResponse(
    val message: String
)


