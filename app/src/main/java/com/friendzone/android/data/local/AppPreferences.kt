package com.friendzone.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.friendzone.android.BuildConfig
import com.friendzone.android.data.remote.dto.ZoneDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "friendzone_prefs")

class AppPreferences(private val context: Context) {
    private val installIdKey = stringPreferencesKey("install_id")
    private val clientIdKey = stringPreferencesKey("client_id")
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val userNameKey = stringPreferencesKey("user_name")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val localZonesKey = stringPreferencesKey("local_zones_json")
    private val localFriendsKey = stringPreferencesKey("local_friends_json")
    private val localInvitationsKey = stringPreferencesKey("local_invitations_json")
    private val currentDeviceLocationKey = stringPreferencesKey("current_device_location_json")
    private val maxMarkersKey = intPreferencesKey("max_markers")
    private val maxRadiusKey = intPreferencesKey("max_radius")
    private val legacyLocationUpdateIntervalMinutesKey = intPreferencesKey("location_update_interval_minutes")
    private val locationUpdateIntervalMinutesKey = stringPreferencesKey("location_update_interval_minutes_decimal")
    private val onlyOwnMarkersKey = booleanPreferencesKey("only_own_markers")
    private val notifyAboutFriendKey = booleanPreferencesKey("notify_about_friend")

    val installId: Flow<String?> = context.dataStore.data.map { it[installIdKey] }
    val clientId: Flow<String?> = context.dataStore.data.map { it[clientIdKey] }
    val apiBaseUrl: Flow<String> = context.dataStore.data.map { it[apiBaseUrlKey] ?: BuildConfig.API_BASE_URL }
    val userName: Flow<String?> = context.dataStore.data.map { it[userNameKey] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[userEmailKey] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[isLoggedInKey] ?: false }
    val currentDeviceLocation: Flow<LocalLocationDto?> =
        context.dataStore.data.map { parseDeviceLocation(it[currentDeviceLocationKey]) }
    val localFriends: Flow<List<LocalFriendDto>> =
        context.dataStore.data.map { parseFriends(it[localFriendsKey] ?: "[]") }
    val maxMarkers: Flow<Int> = context.dataStore.data.map { it[maxMarkersKey] ?: 20 }
    val maxRadius: Flow<Int> = context.dataStore.data.map { it[maxRadiusKey] ?: 2000 }
    val locationUpdateIntervalMinutes: Flow<Double> =
        context.dataStore.data.map { prefs ->
            prefs[locationUpdateIntervalMinutesKey]?.toDoubleOrNull()
                ?: prefs[legacyLocationUpdateIntervalMinutesKey]?.toDouble()
                ?: 1.0
        }
    val onlyOwnMarkers: Flow<Boolean> = context.dataStore.data.map { it[onlyOwnMarkersKey] ?: true }
    val notifyAboutFriend: Flow<Boolean> = context.dataStore.data.map { it[notifyAboutFriendKey] ?: true }

    suspend fun setInstallId(value: String) {
        context.dataStore.edit { it[installIdKey] = value }
    }

    suspend fun setClientId(value: String) {
        context.dataStore.edit { it[clientIdKey] = value }
    }

    suspend fun setApiBaseUrl(value: String) {
        context.dataStore.edit { it[apiBaseUrlKey] = value }
    }

    suspend fun saveUser(name: String, email: String) {
        context.dataStore.edit {
            it[userNameKey] = name
            it[userEmailKey] = email
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { it[isLoggedInKey] = value }
    }

    suspend fun saveMapSettings(
        maxMarkers: Int,
        maxRadius: Int,
        locationUpdateIntervalMinutes: Double,
        onlyOwnMarkers: Boolean,
        notifyAboutFriend: Boolean
    ) {
        context.dataStore.edit {
            it[maxMarkersKey] = maxMarkers
            it[maxRadiusKey] = maxRadius
            it[locationUpdateIntervalMinutesKey] = locationUpdateIntervalMinutes.toString()
            it.remove(legacyLocationUpdateIntervalMinutesKey)
            it[onlyOwnMarkersKey] = onlyOwnMarkers
            it[notifyAboutFriendKey] = notifyAboutFriend
        }
    }

    suspend fun saveCurrentDeviceLocation(location: LocalLocationDto) {
        context.dataStore.edit {
            it[currentDeviceLocationKey] = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("accuracy", location.accuracy)
                put("deviceTimeIso", location.deviceTimeIso)
            }.toString()
        }
    }

    suspend fun getLocalZones(): List<ZoneDto> {
        val raw = context.dataStore.data.map { it[localZonesKey] ?: "[]" }.first()
        return parseZones(raw)
    }

    suspend fun saveLocalZones(zones: List<ZoneDto>) {
        context.dataStore.edit {
            it[localZonesKey] = JSONArray().apply {
                zones.forEach { zone ->
                    put(
                        JSONObject().apply {
                            put("id", zone.id)
                            put("name", zone.name)
                            put("centerLat", zone.centerLat)
                            put("centerLon", zone.centerLon)
                            put("radiusMeters", zone.radiusMeters)
                            put("isActive", zone.isActive)
                            put("detectorFriendIds", JSONArray(zone.detectorFriendIds))
                        }
                    )
                }
            }.toString()
        }
    }

    suspend fun getLocalFriends(): List<LocalFriendDto> {
        val raw = context.dataStore.data.map { it[localFriendsKey] ?: "[]" }.first()
        return parseFriends(raw)
    }

    suspend fun saveLocalFriends(friends: List<LocalFriendDto>) {
        context.dataStore.edit {
            it[localFriendsKey] = JSONArray().apply {
                friends.forEach { friend ->
                    put(
                        JSONObject().apply {
                            put("id", friend.id)
                            put("tag", friend.tag)
                            put("displayName", friend.displayName)
                            put("latitude", friend.latitude)
                            put("longitude", friend.longitude)
                            put("accuracy", friend.accuracy)
                            put("locationUpdatedAtIso", friend.locationUpdatedAtIso)
                        }
                    )
                }
            }.toString()
        }
    }

    suspend fun getLocalInvitations(): List<LocalInvitationDto> {
        val raw = context.dataStore.data.map { it[localInvitationsKey] ?: "[]" }.first()
        return parseInvitations(raw)
    }

    suspend fun saveLocalInvitations(invitations: List<LocalInvitationDto>) {
        context.dataStore.edit {
            it[localInvitationsKey] = JSONArray().apply {
                invitations.forEach { invitation ->
                    put(
                        JSONObject().apply {
                            put("id", invitation.id)
                            put("tag", invitation.tag)
                            put("displayName", invitation.displayName)
                            put("isIncoming", invitation.isIncoming)
                        }
                    )
                }
            }.toString()
        }
    }

    private fun parseZones(raw: String): List<ZoneDto> {
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    add(
                        ZoneDto(
                            id = item.optString("id"),
                            name = item.optString("name"),
                            centerLat = item.optDouble("centerLat"),
                            centerLon = item.optDouble("centerLon"),
                            radiusMeters = item.optDouble("radiusMeters"),
                            isActive = item.optBoolean("isActive"),
                            detectorFriendIds = item.optJSONArray("detectorFriendIds")
                                ?.let { ids ->
                                    buildList {
                                        for (idIndex in 0 until ids.length()) {
                                            add(ids.optString(idIndex))
                                        }
                                    }
                                }
                                ?: emptyList()
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun parseFriends(raw: String): List<LocalFriendDto> {
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    add(
                        LocalFriendDto(
                            id = item.optString("id"),
                            tag = item.optString("tag"),
                            displayName = item.optString("displayName"),
                            latitude = item.optNullableDouble("latitude"),
                            longitude = item.optNullableDouble("longitude"),
                            accuracy = item.optNullableDouble("accuracy"),
                            locationUpdatedAtIso = item.optString("locationUpdatedAtIso")
                                .takeIf { value -> value.isNotBlank() && value != "null" }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun parseDeviceLocation(raw: String?): LocalLocationDto? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(raw)
            LocalLocationDto(
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                accuracy = json.optDouble("accuracy", 0.0),
                deviceTimeIso = json.optString("deviceTimeIso")
            )
        }.getOrNull()
    }

    private fun parseInvitations(raw: String): List<LocalInvitationDto> {
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    add(
                        LocalInvitationDto(
                            id = item.optString("id"),
                            tag = item.optString("tag"),
                            displayName = item.optString("displayName"),
                            isIncoming = item.optBoolean("isIncoming")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject.optNullableDouble(key: String): Double? {
        return if (isNull(key) || !has(key)) null else optDouble(key)
    }
}
