package com.friendzone.android.data.repository

import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.ApiError
import com.friendzone.android.data.remote.dto.AuthLoginRequest
import com.friendzone.android.data.remote.dto.AuthRegisterRequest
import com.friendzone.android.data.remote.dto.AuthUserResponse
import com.friendzone.android.data.remote.dto.PasswordRecoveryRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

class AuthRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiError::class.java)

    suspend fun register(name: String, email: String, password: String): AuthUserResponse {
        val response = runCatching {
            api.registerUser(
                AuthRegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password
                )
            )
        }.getOrElse { throw it.toReadableException() }

        prefs.saveUser(response.name, response.email)
        prefs.setClientId(response.clientId)
        return response
    }

    suspend fun login(email: String, password: String): AuthUserResponse {
        val response = runCatching {
            api.login(
                AuthLoginRequest(
                    email = email.trim(),
                    password = password
                )
            )
        }.getOrElse { throw it.toReadableException() }

        prefs.saveUser(response.name, response.email)
        prefs.setClientId(response.clientId)
        prefs.setLoggedIn(true)
        return response
    }

    suspend fun recoverPassword(email: String): String {
        return runCatching {
            api.recoverPassword(PasswordRecoveryRequest(email.trim())).message
        }.getOrElse { throw it.toReadableException() }
    }

    suspend fun logout() {
        prefs.setLoggedIn(false)
    }

    private fun Throwable.toReadableException(): Exception {
        if (this is HttpException) {
            val parsed = response()?.errorBody()?.string()?.let(errorAdapter::fromJson)
            return Exception(parsed?.message ?: (message ?: "Server error"))
        }
        return Exception(message ?: "Network error")
    }
}


