package com.friendzone.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.friendzone.android.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "friendzone_prefs")

class AppPreferences(private val context: Context) {
    private val installIdKey = stringPreferencesKey("install_id")
    private val clientIdKey = stringPreferencesKey("client_id")
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val userDisplayNameKey = stringPreferencesKey("user_display_name")
    private val userLoginKey = stringPreferencesKey("user_login")
    private val localZonesKey = stringPreferencesKey("local_zones_json")
    private val localFriendsKey = stringPreferencesKey("local_friends_json")
    private val localInvitationsKey = stringPreferencesKey("local_invitations_json")
    private val legacyUserNameKey = stringPreferencesKey("user_name")
    private val legacyUserEmailKey = stringPreferencesKey("user_email")
    private val legacyIsLoggedInKey = booleanPreferencesKey("is_logged_in")
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
    val accessToken: Flow<String?> = context.dataStore.data.map { it[accessTokenKey] }
    val userDisplayName: Flow<String?> = context.dataStore.data.map { it[userDisplayNameKey] }
    val userLogin: Flow<String?> = context.dataStore.data.map { it[userLoginKey] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { !it[accessTokenKey].isNullOrBlank() }
    val currentDeviceLocation: Flow<LocalLocationDto?> =
        context.dataStore.data.map { parseDeviceLocation(it[currentDeviceLocationKey]) }
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

    suspend fun saveAuthSession(
        accessToken: String,
        clientId: String,
        login: String,
        displayName: String?
    ) {
        context.dataStore.edit {
            it[accessTokenKey] = accessToken
            it[clientIdKey] = clientId
            it[userLoginKey] = login
            if (displayName.isNullOrBlank()) {
                it.remove(userDisplayNameKey)
            } else {
                it[userDisplayNameKey] = displayName
            }
            clearLegacyProfileState(it)
        }
    }

    suspend fun saveUserProfile(
        clientId: String,
        login: String,
        displayName: String?
    ) {
        context.dataStore.edit {
            it[clientIdKey] = clientId
            it[userLoginKey] = login
            if (displayName.isNullOrBlank()) {
                it.remove(userDisplayNameKey)
            } else {
                it[userDisplayNameKey] = displayName
            }
        }
    }

    suspend fun clearAuthSession() {
        context.dataStore.edit {
            it.remove(accessTokenKey)
            it.remove(clientIdKey)
            it.remove(userDisplayNameKey)
            it.remove(userLoginKey)
            clearLegacyProfileState(it)
        }
    }

    suspend fun clearLegacyProfileData() {
        context.dataStore.edit { clearLegacyProfileState(it) }
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

    private fun clearLegacyProfileState(preferences: androidx.datastore.preferences.core.MutablePreferences) {
        preferences.remove(localZonesKey)
        preferences.remove(localFriendsKey)
        preferences.remove(localInvitationsKey)
        preferences.remove(legacyUserNameKey)
        preferences.remove(legacyUserEmailKey)
        preferences.remove(legacyIsLoggedInKey)
    }
}
