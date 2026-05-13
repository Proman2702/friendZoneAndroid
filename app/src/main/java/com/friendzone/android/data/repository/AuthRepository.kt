package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.AuthLoginRequest
import com.friendzone.android.data.remote.dto.AuthRegisterRequest
import com.friendzone.android.data.remote.dto.RemoteUserDto

class AuthRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun register(displayName: String, login: String, password: String): RemoteUserDto {
        val response = runCatching {
            api.registerUser(
                AuthRegisterRequest(
                    login = login.trim(),
                    password = password,
                    displayName = displayName.trim().ifBlank { null }
                )
            )
        }.getOrElse { throw it.toReadableException() }

        prefs.saveAuthSession(
            accessToken = response.accessToken,
            clientId = response.user.id,
            login = response.user.login,
            displayName = response.user.displayName
        )
        return response.user
    }

    suspend fun login(login: String, password: String): RemoteUserDto {
        val response = runCatching {
            api.login(
                AuthLoginRequest(
                    login = login.trim(),
                    password = password
                )
            )
        }.getOrElse { throw it.toReadableException() }

        prefs.saveAuthSession(
            accessToken = response.accessToken,
            clientId = response.user.id,
            login = response.user.login,
            displayName = response.user.displayName
        )
        return response.user
    }

    suspend fun getCurrentUser(): RemoteUserDto {
        val user = runCatching { api.getCurrentUser() }
            .getOrElse { throw it.toReadableException() }
        prefs.saveUserProfile(
            clientId = user.id,
            login = user.login,
            displayName = user.displayName
        )
        return user
    }

    suspend fun logout() {
        prefs.clearAuthSession()
    }
}

