package com.friendzone.android.data.repository

import com.friendzone.android.data.remote.FriendZoneApi
import com.friendzone.android.data.remote.dto.ClientRegisterRequest
import kotlinx.coroutines.flow.first
import java.util.UUID

class ClientRepository(
    private val api: FriendZoneApi,
    private val prefs: AppPreferences
) {
    suspend fun ensureRegistered(): Result<Unit> {
        return runCatching {
            val existing = prefs.clientId.first()
            if (!existing.isNullOrBlank()) return@runCatching

            val installId = prefs.installId.first() ?: UUID.randomUUID().toString().also {
                prefs.setInstallId(it)
            }

            val response = api.register(ClientRegisterRequest(installId))
            prefs.setClientId(response.clientId)
        }
    }

    suspend fun getClientId(): String =
        prefs.clientId.first() ?: error("clientId is missing. Registration not completed.")
}


