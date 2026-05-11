package com.friendzone.android.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.repository.FriendsRepository
import com.friendzone.android.data.repository.PlaceSearchRepository
import com.friendzone.android.data.repository.ZoneRepository
import com.friendzone.android.presentation.zones.FriendOptionUi
import com.friendzone.android.presentation.zones.ZoneUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MapFocusTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double = 15.0,
    val requestId: Long = System.currentTimeMillis()
)

data class MapScreenState(
    val isLoading: Boolean = false,
    val zones: List<ZoneUi> = emptyList(),
    val friends: List<FriendOptionUi> = emptyList(),
    val showInactiveZones: Boolean = true,
    val createDialogVisible: Boolean = false,
    val selectedZoneId: String? = null,
    val message: String? = null,
    val focusTarget: MapFocusTarget? = null,
    val maxRadiusMeters: Int = 2000
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val placeSearchRepository: PlaceSearchRepository,
    private val prefs: AppPreferences,
    private val friendsRepository: FriendsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapScreenState())
    val state: StateFlow<MapScreenState> = _state

    private var currentCenterLatitude: Double = 55.751244
    private var currentCenterLongitude: Double = 37.618423

    fun loadZones() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                Triple(
                    zoneRepository.listZones(),
                    friendsRepository.listFriends(),
                    prefs.maxRadius.first()
                )
            }.onSuccess { (zones, friends, maxRadius) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    zones = zones.map { it.toUi() },
                    friends = friends.map { FriendOptionUi(it.id, it.tag, it.displayName) },
                    maxRadiusMeters = maxRadius
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = error.message ?: "Не удалось загрузить зоны"
                )
            }
        }
    }

    fun updateMapCenter(latitude: Double, longitude: Double) {
        currentCenterLatitude = latitude
        currentCenterLongitude = longitude
    }

    fun requestCreateDialog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                createDialogVisible = true,
                selectedZoneId = null,
                maxRadiusMeters = prefs.maxRadius.first(),
                friends = friendsRepository.listFriends().map {
                    FriendOptionUi(it.id, it.tag, it.displayName)
                }
            )
        }
    }

    fun dismissCreateDialog() {
        _state.value = _state.value.copy(createDialogVisible = false)
    }

    fun selectZone(zoneId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                selectedZoneId = zoneId,
                maxRadiusMeters = prefs.maxRadius.first(),
                friends = friendsRepository.listFriends().map {
                    FriendOptionUi(it.id, it.tag, it.displayName)
                }
            )
        }
    }

    fun clearSelectedZone() {
        _state.value = _state.value.copy(selectedZoneId = null)
    }

    fun setShowInactiveZones(value: Boolean) {
        _state.value = _state.value.copy(showInactiveZones = value)
    }

    fun createZone(name: String, radiusMeters: Double, detectorFriendIds: List<String>) {
        viewModelScope.launch {
            val normalizedRadius = radiusMeters.coerceIn(50.0, _state.value.maxRadiusMeters.toDouble())
            runCatching {
                zoneRepository.createZone(
                    name = name.ifBlank { "Новая зона" },
                    centerLat = currentCenterLatitude,
                    centerLon = currentCenterLongitude,
                    radiusMeters = normalizedRadius,
                    isActive = true,
                    detectorFriendIds = detectorFriendIds
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    createDialogVisible = false,
                    message = "Зона добавлена"
                )
                loadZones()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    message = error.message ?: "Не удалось создать зону"
                )
            }
        }
    }

    fun updateZone(zone: ZoneUi) {
        viewModelScope.launch {
            runCatching {
                zoneRepository.updateZone(
                    zoneId = zone.id,
                    name = zone.name,
                    centerLat = zone.centerLat,
                    centerLon = zone.centerLon,
                    radiusMeters = zone.radiusMeters.coerceIn(50.0, _state.value.maxRadiusMeters.toDouble()),
                    isActive = zone.isActive,
                    detectorFriendIds = zone.detectorFriendIds
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    selectedZoneId = null,
                    message = "Зона обновлена"
                )
                loadZones()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    message = error.message ?: "Не удалось обновить зону"
                )
            }
        }
    }

    fun moveZone(zoneId: String, latitude: Double, longitude: Double) {
        val zone = _state.value.zones.firstOrNull { it.id == zoneId } ?: return
        updateZone(zone.copy(centerLat = latitude, centerLon = longitude))
    }

    fun deleteZone(zoneId: String) {
        viewModelScope.launch {
            runCatching { zoneRepository.deleteZone(zoneId) }
                .onSuccess {
                    _state.value = _state.value.copy(
                        selectedZoneId = null,
                        message = "Зона удалена"
                    )
                    loadZones()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        message = error.message ?: "Не удалось удалить зону"
                    )
                }
        }
    }

    fun searchPlace(query: String) {
        viewModelScope.launch {
            runCatching { placeSearchRepository.search(query) }
                .onSuccess { result ->
                    if (result == null) {
                        _state.value = _state.value.copy(message = "Место не найдено")
                    } else {
                        currentCenterLatitude = result.latitude
                        currentCenterLongitude = result.longitude
                        _state.value = _state.value.copy(
                            message = result.address,
                            focusTarget = MapFocusTarget(
                                latitude = result.latitude,
                                longitude = result.longitude
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        message = error.message ?: "Поиск места недоступен"
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
            detectorFriendIds = detectorFriendIds
        )
    }
}
