package com.friendzone.android.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.repository.PlaceSearchRepository
import com.friendzone.android.data.repository.ZoneRepository
import com.friendzone.android.presentation.zones.ZoneUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MapFocusTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double = 15.0,
    // Идентификатор нужен, чтобы карта не анимировалась повторно на каждом recomposition.
    val requestId: Long = System.currentTimeMillis()
)

data class MapScreenState(
    val isLoading: Boolean = false,
    val zones: List<ZoneUi> = emptyList(),
    val showInactiveZones: Boolean = true,
    val createDialogVisible: Boolean = false,
    val selectedZoneId: String? = null,
    val message: String? = null,
    val focusTarget: MapFocusTarget? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val zoneRepository: ZoneRepository,
    private val placeSearchRepository: PlaceSearchRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MapScreenState())
    val state: StateFlow<MapScreenState> = _state

    private var currentCenterLatitude: Double = 55.751244
    private var currentCenterLongitude: Double = 37.618423

    fun loadZones() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { zoneRepository.listZones() }
                .onSuccess { zones ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        zones = zones.map { it.toUi() }
                    )
                }
                .onFailure { error ->
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
        _state.value = _state.value.copy(createDialogVisible = true, selectedZoneId = null)
    }

    fun dismissCreateDialog() {
        _state.value = _state.value.copy(createDialogVisible = false)
    }

    fun selectZone(zoneId: String) {
        _state.value = _state.value.copy(selectedZoneId = zoneId)
    }

    fun clearSelectedZone() {
        _state.value = _state.value.copy(selectedZoneId = null)
    }

    fun setShowInactiveZones(value: Boolean) {
        _state.value = _state.value.copy(showInactiveZones = value)
    }

    fun createZone(name: String, radiusMeters: Double) {
        viewModelScope.launch {
            // Ставим нижнюю границу, чтобы зона не исчезала визуально на карте.
            val normalizedRadius = radiusMeters.coerceAtLeast(50.0)
            runCatching {
                zoneRepository.createZone(
                    name = name.ifBlank { "Новая зона" },
                    centerLat = currentCenterLatitude,
                    centerLon = currentCenterLongitude,
                    radiusMeters = normalizedRadius,
                    isActive = true
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
                    radiusMeters = zone.radiusMeters.coerceAtLeast(50.0),
                    isActive = zone.isActive
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
        // Перетаскивание маркера сохраняем как обычное обновление зоны.
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
            isActive = isActive
        )
    }
}
