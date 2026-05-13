package com.friendzone.android.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.repository.AuthRepository
import com.friendzone.android.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AccountState(
    val userName: String = "Guest",
    val userLogin: String = "",
    val history: List<AccountHistoryItem> = emptyList(),
    val message: String? = null
)

data class AccountHistoryItem(
    val id: String,
    val title: String,
    val subtitle: String
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state

    fun load() {
        viewModelScope.launch {
            val cachedName = prefs.userDisplayName.first()?.takeIf(String::isNotBlank)
                ?: prefs.userLogin.first()
                ?: "Guest"
            val cachedLogin = prefs.userLogin.first().orEmpty()
            _state.value = _state.value.copy(
                userName = cachedName,
                userLogin = cachedLogin,
                message = null
            )

            runCatching {
                val user = authRepository.getCurrentUser()
                val events = eventRepository.getEvents()
                AccountState(
                    userName = user.displayName?.takeIf(String::isNotBlank) ?: user.login,
                    userLogin = user.login,
                    history = events.mapIndexed { index, event ->
                        AccountHistoryItem(
                            id = "${event.zoneId}:$index:${event.eventTime.orEmpty()}",
                            title = "${event.type} ${event.zoneName}",
                            subtitle = event.eventTime ?: (event.actorLogin ?: "No details")
                        )
                    }
                )
            }.onSuccess {
                _state.value = it
            }.onFailure {
                _state.value = _state.value.copy(message = it.message ?: "Failed to load account")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
