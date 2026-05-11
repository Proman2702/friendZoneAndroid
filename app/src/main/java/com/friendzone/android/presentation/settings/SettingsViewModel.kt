package com.friendzone.android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsState(
    val apiBaseUrl: String = "",
    val maxMarkers: String = "20",
    val maxRadius: String = "2000",
    val onlyOwnMarkers: Boolean = true,
    val notifyAboutFriend: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun load() {
        viewModelScope.launch {
            val apiBaseUrl = prefs.apiBaseUrl.first()
            val maxMarkers = prefs.maxMarkers.first()
            val maxRadius = prefs.maxRadius.first()
            val onlyOwnMarkers = prefs.onlyOwnMarkers.first()
            val notifyAboutFriend = prefs.notifyAboutFriend.first()
            _state.value = SettingsState(
                apiBaseUrl = apiBaseUrl,
                maxMarkers = maxMarkers.toString(),
                maxRadius = maxRadius.toString(),
                onlyOwnMarkers = onlyOwnMarkers,
                notifyAboutFriend = notifyAboutFriend
            )
        }
    }

    fun updateApiBaseUrl(value: String) {
        _state.value = _state.value.copy(apiBaseUrl = value)
    }

    fun updateMaxMarkers(value: String) {
        _state.value = _state.value.copy(maxMarkers = value.filter { it.isDigit() })
    }

    fun updateMaxRadius(value: String) {
        _state.value = _state.value.copy(maxRadius = value.filter { it.isDigit() })
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
            prefs.setApiBaseUrl(normalized)
            prefs.saveMapSettings(
                maxMarkers = _state.value.maxMarkers.toIntOrNull()?.coerceAtLeast(1) ?: 20,
                maxRadius = _state.value.maxRadius.toIntOrNull()?.coerceAtLeast(50) ?: 2000,
                onlyOwnMarkers = _state.value.onlyOwnMarkers,
                notifyAboutFriend = _state.value.notifyAboutFriend
            )
            _state.value = _state.value.copy(apiBaseUrl = normalized)
        }
    }
}
