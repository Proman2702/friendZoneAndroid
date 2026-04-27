package com.friendzone.android.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.friendzone.android.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "friendzone_prefs")

class AppPreferences(private val context: Context) {
    private val installIdKey = stringPreferencesKey("install_id")
    private val clientIdKey = stringPreferencesKey("client_id")
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val userNameKey = stringPreferencesKey("user_name")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")

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
}
