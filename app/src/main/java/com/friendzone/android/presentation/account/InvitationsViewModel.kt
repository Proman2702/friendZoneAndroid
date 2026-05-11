package com.friendzone.android.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InvitationUi(
    val id: String,
    val tag: String,
    val displayName: String
)

data class InvitationsState(
    val invited: List<InvitationUi> = emptyList(),
    val incoming: List<InvitationUi> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class InvitationsViewModel @Inject constructor(
    private val repository: FriendsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(InvitationsState())
    val state: StateFlow<InvitationsState> = _state

    fun load() {
        viewModelScope.launch {
            val invitations = repository.listInvitations()
            _state.value = InvitationsState(
                invited = invitations.filterNot { it.isIncoming }.map {
                    InvitationUi(it.id, it.tag, it.displayName)
                },
                incoming = invitations.filter { it.isIncoming }.map {
                    InvitationUi(it.id, it.tag, it.displayName)
                }
            )
        }
    }

    fun sendInvitation(tag: String) {
        if (tag.trim().isBlank()) {
            _state.value = _state.value.copy(message = "Введите тег пользователя")
            return
        }
        viewModelScope.launch {
            repository.sendInvitation(tag)
            _state.value = _state.value.copy(message = "Приглашение сохранено локально")
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
