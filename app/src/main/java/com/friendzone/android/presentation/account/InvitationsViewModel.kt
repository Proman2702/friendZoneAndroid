package com.friendzone.android.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.remote.dto.FriendRequestDto
import com.friendzone.android.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InvitationUi(
    val id: String,
    val login: String,
    val displayName: String,
    val status: String
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
            runCatching {
                InvitationsState(
                    invited = repository.listOutgoingInvitations().map { it.toUi(isIncoming = false) },
                    incoming = repository.listIncomingInvitations().map { it.toUi(isIncoming = true) }
                )
            }.onSuccess {
                _state.value = it
            }.onFailure {
                _state.value = _state.value.copy(message = it.message ?: "Failed to load invitations")
            }
        }
    }

    fun sendInvitation(login: String) {
        if (login.trim().isBlank()) {
            _state.value = _state.value.copy(message = "Enter user login")
            return
        }

        viewModelScope.launch {
            runCatching { repository.sendInvitation(login) }
                .onSuccess {
                    _state.value = _state.value.copy(message = "Invitation sent")
                    load()
                }
                .onFailure {
                    _state.value = _state.value.copy(message = it.message ?: "Failed to send invitation")
                }
        }
    }

    fun acceptInvitation(requestId: String) {
        viewModelScope.launch {
            runCatching { repository.acceptInvitation(requestId) }
                .onSuccess {
                    _state.value = _state.value.copy(message = "Invitation accepted")
                    load()
                }
                .onFailure {
                    _state.value = _state.value.copy(message = it.message ?: "Failed to accept invitation")
                }
        }
    }

    fun declineInvitation(requestId: String) {
        viewModelScope.launch {
            runCatching { repository.declineInvitation(requestId) }
                .onSuccess {
                    _state.value = _state.value.copy(message = "Invitation declined")
                    load()
                }
                .onFailure {
                    _state.value = _state.value.copy(message = it.message ?: "Failed to decline invitation")
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun FriendRequestDto.toUi(isIncoming: Boolean): InvitationUi {
        val user = if (isIncoming) requester else addressee
        return InvitationUi(
            id = id,
            login = user.login,
            displayName = user.displayName?.takeIf(String::isNotBlank) ?: user.login,
            status = status
        )
    }
}
