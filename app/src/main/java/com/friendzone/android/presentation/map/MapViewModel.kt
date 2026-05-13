package com.friendzone.android.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.local.LocalFriendDto
import com.friendzone.android.data.local.LocalLocationDto
import com.friendzone.android.data.repository.FriendsRepository
import com.friendzone.android.data.repository.LocationRepository
import com.friendzone.android.data.repository.PlaceSearchRepository
import com.friendzone.android.data.repository.ZoneRepository
import com.friendzone.android.presentation.zones.FriendOptionUi
import com.friendzone.android.presentation.zones.ZoneUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class MapFocusTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double = 15.0,
    val requestId: Long = System.currentTimeMillis()
)

data class DeviceLocationUi(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val deviceTimeIso: String,
    val zoneNames: List<String>
)

data class FriendMapUi(
    val id: String,
    val displayName: String,
    val tag: String,
    val latitude: Double,
    val longitude: Double,
    val zoneNames: List<String>,
    val locationUpdatedAtIso: String?
)

data class MapScreenState(
    val isLoading: Boolean = false,
    val zones: List<ZoneUi> = emptyList(),
    val friends: List<FriendOptionUi> = emptyList(),
    val friendMarkers: List<FriendMapUi> = emptyList(),
    val deviceLocation: DeviceLocationUi? = null,
    val showInactiveZones: Boolean = true,
    val createDialogVisible: Boolean = false,
    val selectedZoneId: String? = null,
    val message: String? = null,
    val focusTarget: MapFocusTarget? = null,
    val maxRadiusMeters: Int = 2000,
    val onlyOwnMarkers: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val placeSearchRepository: PlaceSearchRepository,
    private val locationRepository: LocationRepository,
    private val prefs: AppPreferences,
    private val friendsRepository: FriendsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapScreenState())
    val state: StateFlow<MapScreenState> = _state

    private var currentCenterLatitude: Double = 55.751244
    private var currentCenterLongitude: Double = 37.618423
    private var initialDeviceFocusApplied = false

    init {
        observeBindings()
    }

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
                applyDerivedMarkers()
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
                    message = "Зона создана"
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
                            focusTarget = MapFocusTarget(result.latitude, result.longitude)
                        )
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        message = error.message ?: "Поиск недоступен"
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun observeBindings() {
        viewModelScope.launch {
            combine(
                locationRepository.currentDeviceLocation,
                friendsRepository.observeFriends(),
                prefs.onlyOwnMarkers
            ) { deviceLocation, friends, onlyOwnMarkers ->
                Triple(deviceLocation, friends, onlyOwnMarkers)
            }.collect { (deviceLocation, friends, onlyOwnMarkers) ->
                _state.value = _state.value.copy(
                    friends = friends.map { FriendOptionUi(it.id, it.tag, it.displayName) },
                    onlyOwnMarkers = onlyOwnMarkers
                )
                applyDerivedMarkers(deviceLocation, friends, onlyOwnMarkers)
            }
        }
    }

    private fun applyDerivedMarkers(
        deviceLocation: LocalLocationDto? = null,
        friends: List<LocalFriendDto> = emptyList(),
        onlyOwnMarkers: Boolean = _state.value.onlyOwnMarkers
    ) {
        val actualDeviceLocation = deviceLocation ?: _state.value.deviceLocation?.let {
            LocalLocationDto(it.latitude, it.longitude, it.accuracy, it.deviceTimeIso)
        }
        val actualFriends = if (friends.isEmpty()) {
            _state.value.friendMarkers.map {
                LocalFriendDto(
                    id = it.id,
                    tag = it.tag,
                    displayName = it.displayName,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    locationUpdatedAtIso = it.locationUpdatedAtIso
                )
            }
        } else {
            friends
        }

        val deviceUi = actualDeviceLocation?.toUi(_state.value.zones)
        val friendMarkers = if (onlyOwnMarkers) {
            emptyList()
        } else {
            actualFriends.mapNotNull { it.toMapUi(_state.value.zones) }
        }

        _state.value = _state.value.copy(
            deviceLocation = deviceUi,
            friendMarkers = friendMarkers
        )

        if (deviceUi != null && !initialDeviceFocusApplied && _state.value.focusTarget == null) {
            currentCenterLatitude = deviceUi.latitude
            currentCenterLongitude = deviceUi.longitude
            initialDeviceFocusApplied = true
            _state.value = _state.value.copy(
                focusTarget = MapFocusTarget(deviceUi.latitude, deviceUi.longitude, 16.0)
            )
        }
    }

    private fun LocalLocationDto.toUi(zones: List<ZoneUi>): DeviceLocationUi {
        return DeviceLocationUi(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            deviceTimeIso = deviceTimeIso,
            zoneNames = zones.filter { it.containsPoint(latitude, longitude) }.map { it.name }
        )
    }

    private fun LocalFriendDto.toMapUi(zones: List<ZoneUi>): FriendMapUi? {
        val lat = latitude ?: return null
        val lon = longitude ?: return null
        return FriendMapUi(
            id = id,
            displayName = displayName,
            tag = tag,
            latitude = lat,
            longitude = lon,
            zoneNames = zones
                .filter { zone -> zone.containsPoint(lat, lon) && (zone.detectorFriendIds.isEmpty() || zone.detectorFriendIds.contains(id)) }
                .map { it.name },
            locationUpdatedAtIso = locationUpdatedAtIso
        )
    }

    private fun ZoneUi.containsPoint(latitude: Double, longitude: Double): Boolean {
        return distanceMeters(centerLat, centerLon, latitude, longitude) <= radiusMeters
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0
        val cosine = (
            (sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2))) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(Math.toRadians(lon1 - lon2)))
            )
        val angle = acos(min(1.0, max(-1.0, cosine)))
        return earthRadius * angle
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
