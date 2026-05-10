package com.friendzone.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    val installId: Flow<String?> = context.dataStore.data.map { it[installIdKey] }
    val clientId: Flow<String?> = context.dataStore.data.map { it[clientIdKey] }
    val apiBaseUrl: Flow<String> = context.dataStore.data.map { it[apiBaseUrlKey] ?: BuildConfig.API_BASE_URL }
    val userName: Flow<String?> = context.dataStore.data.map { it[userNameKey] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[userEmailKey] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[isLoggedInKey] ?: false }

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

    suspend fun getLocalZones(): List<ZoneDto> {
        // Временно храним зоны одним JSON-массивом, чтобы не поднимать отдельную БД.
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
                        }
                    )
                }
            }.toString()
        }
    }

    private fun parseZones(raw: String): List<ZoneDto> {
        return runCatching {
            // Если JSON повредится, просто вернём пустой список и не уроним экран карты.
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
                            isActive = item.optBoolean("isActive")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}


