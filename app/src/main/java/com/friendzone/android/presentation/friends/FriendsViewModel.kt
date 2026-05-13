package com.friendzone.android.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendUi(
    val id: String,
    val displayName: String,
    val tag: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationUpdatedAtIso: String? = null
)

data class FriendsState(
    val friends: List<FriendUi> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                friends = repository.listFriends().map {
                    FriendUi(
                        id = it.id,
                        displayName = it.displayName,
                        tag = it.tag,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        locationUpdatedAtIso = it.locationUpdatedAtIso
                    )
                }
            )
        }
    }

    fun addFriend(tag: String) {
        if (tag.trim().isBlank()) {
            _state.value = _state.value.copy(message = "Введите тег друга")
            return
        }
        viewModelScope.launch {
            repository.addFriend(tag)
            _state.value = _state.value.copy(message = "Друг добавлен")
            load()
        }
    }

    fun renameFriend(friendId: String, name: String) {
        if (name.trim().isBlank()) {
            _state.value = _state.value.copy(message = "Имя не может быть пустым")
            return
        }
        viewModelScope.launch {
            repository.renameFriend(friendId, name)
            _state.value = _state.value.copy(message = "Имя обновлено")
            load()
        }
    }

    fun updateFriendLocation(friendId: String, latitude: String, longitude: String) {
        val lat = latitude.trim()
        val lon = longitude.trim()
        if (lat.isBlank() && lon.isBlank()) {
            viewModelScope.launch {
                repository.updateFriendLocation(friendId, null, null)
                _state.value = _state.value.copy(message = "Геолокация друга очищена")
                load()
            }
            return
        }

        val parsedLat = lat.toDoubleOrNull()
        val parsedLon = lon.toDoubleOrNull()
        if (parsedLat == null || parsedLon == null) {
            _state.value = _state.value.copy(message = "Координаты должны быть числами")
            return
        }

        viewModelScope.launch {
            repository.updateFriendLocation(
                friendId = friendId,
                latitude = parsedLat,
                longitude = parsedLon,
                deviceTimeIso = Instant.now().toString()
            )
            _state.value = _state.value.copy(message = "Геолокация друга сохранена")
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
