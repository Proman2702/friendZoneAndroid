package com.friendzone.android.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AccountState(
    val userName: String = "Гость",
    val userEmail: String = "guest@friendzone.local"
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = AccountState(
                userName = prefs.userName.first() ?: "Гость",
                userEmail = prefs.userEmail.first() ?: "guest@friendzone.local"
            )
        }
    }
}
