package com.friendzone.android.data.repository

import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.local.LocalFriendDto
import com.friendzone.android.data.local.LocalInvitationDto
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class FriendsRepository(
    private val prefs: AppPreferences
) {
    fun observeFriends(): Flow<List<LocalFriendDto>> = prefs.localFriends

    suspend fun listFriends(): List<LocalFriendDto> {
        return prefs.getLocalFriends()
    }

    suspend fun addFriend(tag: String): LocalFriendDto {
        val normalizedTag = normalizeTag(tag)
        val existing = prefs.getLocalFriends().firstOrNull { it.tag.equals(normalizedTag, ignoreCase = true) }
        if (existing != null) {
            return existing
        }

        val friend = LocalFriendDto(
            id = UUID.randomUUID().toString(),
            tag = normalizedTag,
            displayName = normalizedTag.removePrefix("@")
        )
        prefs.saveLocalFriends(prefs.getLocalFriends() + friend)
        return friend
    }

    suspend fun renameFriend(friendId: String, displayName: String) {
        val updated = prefs.getLocalFriends().map { friend ->
            if (friend.id == friendId) friend.copy(displayName = displayName.trim()) else friend
        }
        prefs.saveLocalFriends(updated)
    }

    suspend fun updateFriendLocation(
        friendId: String,
        latitude: Double?,
        longitude: Double?,
        accuracy: Double? = null,
        deviceTimeIso: String? = null
    ) {
        val updated = prefs.getLocalFriends().map { friend ->
            if (friend.id == friendId) {
                friend.copy(
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = accuracy,
                    locationUpdatedAtIso = if (latitude != null && longitude != null) deviceTimeIso else null
                )
            } else {
                friend
            }
        }
        prefs.saveLocalFriends(updated)
    }

    suspend fun listInvitations(): List<LocalInvitationDto> {
        return prefs.getLocalInvitations()
    }

    suspend fun sendInvitation(tag: String): LocalInvitationDto {
        val normalizedTag = normalizeTag(tag)
        val invitations = prefs.getLocalInvitations()
        val existing = invitations.firstOrNull {
            !it.isIncoming && it.tag.equals(normalizedTag, ignoreCase = true)
        }
        if (existing != null) {
            return existing
        }

        val invitation = LocalInvitationDto(
            id = UUID.randomUUID().toString(),
            tag = normalizedTag,
            displayName = normalizedTag.removePrefix("@"),
            isIncoming = false
        )
        prefs.saveLocalInvitations(invitations + invitation)
        return invitation
    }

    private fun normalizeTag(tag: String): String {
        val compact = tag.trim().removePrefix("@").replace(" ", "")
        return "@$compact"
    }
}
