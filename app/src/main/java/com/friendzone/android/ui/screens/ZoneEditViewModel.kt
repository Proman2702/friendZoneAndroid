package com.friendzone.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZoneEditState(
    val isLoading: Boolean = false,
    val zoneId: String? = null,
    val name: String = "",
    val centerLat: Double = 55.751244,
    val centerLon: Double = 37.618423,
    val radiusMeters: Double = 300.0,
    val isActive: Boolean = true,
    val zones: List<ZoneOverlayUi> = emptyList()
)

data class ZoneOverlayUi(
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLon: Double,
    val radiusMeters: Double,
    val isActive: Boolean
)

@HiltViewModel
class ZoneEditViewModel @Inject constructor(
    private val repository: ZoneRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ZoneEditState())
    val state: StateFlow<ZoneEditState> = _state

    fun load(zoneId: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { repository.listZones() }
                .onSuccess { zone ->
                    val zones = zone.map {
                        ZoneOverlayUi(
                            id = it.id,
                            name = it.name,
                            centerLat = it.centerLat,
                            centerLon = it.centerLon,
                            radiusMeters = it.radiusMeters,
                            isActive = it.isActive
                        )
                    }
                    if (zoneId != null) {
                        val current = zone.firstOrNull { it.id == zoneId }
                        if (current != null) {
                            _state.value = ZoneEditState(
                                isLoading = false,
                                zoneId = current.id,
                                name = current.name,
                                centerLat = current.centerLat,
                                centerLon = current.centerLon,
                                radiusMeters = current.radiusMeters,
                                isActive = current.isActive,
                                zones = zones
                            )
                            return@onSuccess
                        }
                    }
                    _state.value = _state.value.copy(isLoading = false, zones = zones)
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }

    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun updateCenter(lat: Double, lon: Double) {
        _state.value = _state.value.copy(centerLat = lat, centerLon = lon)
    }

    fun updateRadius(value: String) {
        val parsed = value.toDoubleOrNull() ?: _state.value.radiusMeters
        _state.value = _state.value.copy(radiusMeters = parsed)
    }

    fun updateActive(value: Boolean) {
        _state.value = _state.value.copy(isActive = value)
    }

    fun save(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            val state = _state.value
            val result = runCatching {
                if (state.zoneId == null) {
                    repository.createZone(
                        name = state.name,
                        centerLat = state.centerLat,
                        centerLon = state.centerLon,
                        radiusMeters = state.radiusMeters,
                        isActive = state.isActive
                    )
                } else {
                    repository.updateZone(
                        zoneId = state.zoneId,
                        name = state.name,
                        centerLat = state.centerLat,
                        centerLon = state.centerLon,
                        radiusMeters = state.radiusMeters,
                        isActive = state.isActive
                    )
                }
            }
            result.onSuccess { onSuccess() }
                .onFailure { onError(it) }
        }
    }

    fun delete(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val zoneId = _state.value.zoneId ?: return
        viewModelScope.launch {
            runCatching { repository.deleteZone(zoneId) }
                .onSuccess { onSuccess() }
                .onFailure { onError(it) }
        }
    }
}
