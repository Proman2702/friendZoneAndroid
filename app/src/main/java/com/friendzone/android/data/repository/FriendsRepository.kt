package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.CreateFriendRequest
import com.friendzone.android.data.remote.dto.FriendRequestDto
import com.friendzone.android.data.remote.dto.RemoteUserDto
import kotlinx.coroutines.flow.first

class FriendsRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun listFriends(): List<RemoteUserDto> {
        ensureLoggedIn()
        return runCatching { api.getFriends() }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun removeFriend(friendId: String) {
        ensureLoggedIn()
        runCatching { api.removeFriend(friendId) }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun listIncomingInvitations(): List<FriendRequestDto> {
        ensureLoggedIn()
        return runCatching { api.getIncomingFriendRequests() }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun listOutgoingInvitations(): List<FriendRequestDto> {
        ensureLoggedIn()
        return runCatching { api.getOutgoingFriendRequests() }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun sendInvitation(login: String): FriendRequestDto {
        ensureLoggedIn()
        return runCatching {
            api.sendFriendRequest(CreateFriendRequest(normalizeLogin(login)))
        }.getOrElse { throw it.toReadableException() }
    }

    suspend fun acceptInvitation(requestId: String): FriendRequestDto {
        ensureLoggedIn()
        return runCatching { api.acceptFriendRequest(requestId) }
            .getOrElse { throw it.toReadableException() }
    }

    suspend fun declineInvitation(requestId: String): FriendRequestDto {
        ensureLoggedIn()
        return runCatching { api.declineFriendRequest(requestId) }
            .getOrElse { throw it.toReadableException() }
    }

    private suspend fun ensureLoggedIn() {
        if (prefs.accessToken.first().isNullOrBlank()) {
            error("Login required")
        }
    }

    private fun normalizeLogin(login: String): String {
        return login.trim().removePrefix("@")
    }
}
