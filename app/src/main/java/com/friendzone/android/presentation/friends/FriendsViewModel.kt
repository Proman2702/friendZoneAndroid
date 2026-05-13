package com.friendzone.android.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendUi(
    val id: String,
    val displayName: String,
    val login: String
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
            runCatching { repository.listFriends() }
                .onSuccess { friends ->
                    _state.value = _state.value.copy(
                        friends = friends.map {
                            FriendUi(
                                id = it.id,
                                displayName = it.displayName?.takeIf(String::isNotBlank) ?: it.login,
                                login = it.login
                            )
                        }
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(message = it.message ?: "Failed to load friends")
                }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            runCatching { repository.removeFriend(friendId) }
                .onSuccess {
                    _state.value = _state.value.copy(message = "Friend removed")
                    load()
                }
                .onFailure {
                    _state.value = _state.value.copy(message = it.message ?: "Failed to remove friend")
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
