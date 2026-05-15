package com.friendzone.android.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.friendzone.android.core.location.LocationTrackingManager
import com.friendzone.android.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsState(
    val apiBaseUrl: String = "",
    val maxMarkers: String = "20",
    val maxRadius: String = "2000",
    val locationUpdateIntervalMinutes: String = "1.0",
    val onlyOwnMarkers: Boolean = true,
    val notifyAboutFriend: Boolean = true
)

class SettingsViewModel(
    private val prefs: AppPreferences,
    private val appContext: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = SettingsState(
                apiBaseUrl = prefs.apiBaseUrl.first(),
                maxMarkers = prefs.maxMarkers.first().toString(),
                maxRadius = prefs.maxRadius.first().toString(),
                locationUpdateIntervalMinutes = prefs.locationUpdateIntervalMinutes.first().toString(),
                onlyOwnMarkers = prefs.onlyOwnMarkers.first(),
                notifyAboutFriend = prefs.notifyAboutFriend.first()
            )
        }
    }

    fun updateApiBaseUrl(value: String) {
        _state.value = _state.value.copy(apiBaseUrl = value)
    }

    fun updateMaxMarkers(value: String) {
        _state.value = _state.value.copy(maxMarkers = value.filter(Char::isDigit))
    }

    fun updateMaxRadius(value: String) {
        _state.value = _state.value.copy(maxRadius = value.filter(Char::isDigit))
    }

    fun updateLocationUpdateIntervalMinutes(value: String) {
        val normalized = buildString {
            var dotSeen = false
            value.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !dotSeen -> {
                        append(char)
                        dotSeen = true
                    }
                    char == ',' && !dotSeen -> {
                        append('.')
                        dotSeen = true
                    }
                }
            }
        }
        _state.value = _state.value.copy(locationUpdateIntervalMinutes = normalized)
    }

    fun updateOnlyOwnMarkers(value: Boolean) {
        _state.value = _state.value.copy(onlyOwnMarkers = value)
    }

    fun updateNotifyAboutFriend(value: Boolean) {
        _state.value = _state.value.copy(notifyAboutFriend = value)
    }

    fun save() {
        viewModelScope.launch {
            val trimmed = _state.value.apiBaseUrl.trim()
            val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "http://$trimmed"
            }
            val normalized = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
            val locationIntervalMinutes =
                _state.value.locationUpdateIntervalMinutes.toDoubleOrNull()?.coerceAtLeast(0.1) ?: 1.0

            prefs.setApiBaseUrl(normalized)
            prefs.saveMapSettings(
                maxMarkers = _state.value.maxMarkers.toIntOrNull()?.coerceAtLeast(1) ?: 20,
                maxRadius = _state.value.maxRadius.toIntOrNull()?.coerceAtLeast(50) ?: 2000,
                locationUpdateIntervalMinutes = locationIntervalMinutes,
                onlyOwnMarkers = _state.value.onlyOwnMarkers,
                notifyAboutFriend = _state.value.notifyAboutFriend
            )
            LocationTrackingManager.restart(appContext, locationIntervalMinutes)
            _state.value = _state.value.copy(
                apiBaseUrl = normalized,
                locationUpdateIntervalMinutes = locationIntervalMinutes.toString()
            )
        }
    }

    class Factory(
        private val prefs: AppPreferences,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == SettingsViewModel::class.java)
            return SettingsViewModel(prefs, appContext) as T
        }
    }
}
