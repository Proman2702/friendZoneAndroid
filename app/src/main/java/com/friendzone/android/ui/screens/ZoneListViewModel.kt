package com.friendzone.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.ZoneRepository
import com.friendzone.android.data.AppPreferences
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZoneUi(
    val id: String,
    val name: String,
    val radiusMeters: Double,
    val isActive: Boolean
)

data class ZoneListState(
    val isLoading: Boolean = false,
    val zones: List<ZoneUi> = emptyList(),
    val error: String? = null,
    val apiBaseUrl: String = ""
)

@HiltViewModel
class ZoneListViewModel @Inject constructor(
    private val repository: ZoneRepository,
    private val prefs: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(ZoneListState())
    val state: StateFlow<ZoneListState> = _state

    fun loadBaseUrl() {
        viewModelScope.launch {
            val url = prefs.apiBaseUrl.first()
            _state.value = _state.value.copy(apiBaseUrl = url)
        }
    }

    fun updateBaseUrl(value: String) {
        viewModelScope.launch {
            val trimmed = value.trim()
            val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "http://$trimmed"
            }
            val normalized = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
            prefs.setApiBaseUrl(normalized)
            _state.value = _state.value.copy(apiBaseUrl = normalized)
        }
    }

    fun loadZones() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { repository.listZones() }
                .onSuccess { zones ->
                    _state.value = ZoneListState(
                        isLoading = false,
                        zones = zones.map {
                            ZoneUi(
                                id = it.id,
                                name = it.name,
                                radiusMeters = it.radiusMeters,
                                isActive = it.isActive
                            )
                        },
                        apiBaseUrl = _state.value.apiBaseUrl
                    )
                }
                .onFailure { error ->
                    _state.value = ZoneListState(
                        isLoading = false,
                        error = error.message ?: "Failed to load zones",
                        apiBaseUrl = _state.value.apiBaseUrl
                    )
                }
        }
    }
}
