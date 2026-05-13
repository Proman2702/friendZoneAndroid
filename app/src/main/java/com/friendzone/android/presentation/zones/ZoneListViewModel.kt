package com.friendzone.android.presentation.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.repository.FriendsRepository
import com.friendzone.android.data.repository.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ZoneUi(
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean,
    val detectorFriendIds: List<String>
)

data class ZoneListState(
    val isLoading: Boolean = false,
    val zones: List<ZoneUi> = emptyList(),
    val friends: List<FriendOptionUi> = emptyList(),
    val error: String? = null,
    val apiBaseUrl: String = "",
    val message: String? = null,
    val maxRadiusMeters: Int = 2000
)

@HiltViewModel
class ZoneListViewModel @Inject constructor(
    private val repository: ZoneRepository,
    private val prefs: AppPreferences,
    private val friendsRepository: FriendsRepository
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
            runCatching {
                Triple(
                    repository.listZones(),
                    friendsRepository.listFriends(),
                    prefs.maxRadius.first()
                )
            }.onSuccess { (zones, friends, maxRadius) ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        zones = zones.map { it.toUi() },
                        friends = friends.map {
                            FriendOptionUi(
                                id = it.id,
                                tag = it.login,
                                displayName = it.displayName?.takeIf(String::isNotBlank) ?: it.login
                            )
                        },
                        maxRadiusMeters = maxRadius
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить зоны"
                    )
                }
        }
    }

    fun updateZone(zone: ZoneUi) {
        viewModelScope.launch {
            runCatching {
                repository.updateZone(
                    zoneId = zone.id,
                    name = zone.name,
                    centerLat = zone.centerLat,
                    centerLon = zone.centerLon,
                    radiusMeters = zone.radiusMeters.coerceIn(50.0, _state.value.maxRadiusMeters.toDouble()),
                    isActive = zone.isActive,
                    detectorFriendIds = zone.detectorFriendIds
                )
            }.onSuccess {
                _state.value = _state.value.copy(message = "Зона обновлена")
                loadZones()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    message = error.message ?: "Не удалось обновить зону"
                )
            }
        }
    }

    fun deleteZone(zoneId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteZone(zoneId) }
                .onSuccess {
                    _state.value = _state.value.copy(message = "Зона удалена")
                    loadZones()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        message = error.message ?: "Не удалось удалить зону"
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun com.friendzone.android.data.remote.dto.ZoneDto.toUi(): ZoneUi {
        return ZoneUi(
            id = id,
            name = name,
            centerLat = centerLat,
            centerLon = centerLon,
            radiusMeters = radiusMeters,
            isActive = isActive,
            detectorFriendIds = notifyFriendIds
        )
    }
}
