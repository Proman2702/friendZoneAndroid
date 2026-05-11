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
    val tag: String
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
            val friends = repository.listFriends()
            _state.value = _state.value.copy(
                friends = friends.map { FriendUi(it.id, it.displayName, it.tag) }
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
            _state.value = _state.value.copy(message = "Друг добавлен локально")
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
            _state.value = _state.value.copy(message = "Имя друга обновлено")
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
