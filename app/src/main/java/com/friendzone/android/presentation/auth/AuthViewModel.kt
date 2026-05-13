package com.friendzone.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.local.AppPreferences
import com.friendzone.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val error = MutableStateFlow<String?>(null)
    private val info = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            prefs.clearLegacyProfileData()
        }
    }

    val state: StateFlow<AuthUiState> = combine(prefs.isLoggedIn, error, info) { isLoggedIn, errorText, infoText ->
        AuthUiState(isLoggedIn, errorText, infoText)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthUiState())

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            error.value = "Fill in login and password"
            info.value = null
            return
        }

        viewModelScope.launch {
            error.value = null
            info.value = null
            runCatching { authRepository.login(login, password) }
                .onFailure {
                    error.value = it.message ?: "Login failed"
                }
        }
    }

    fun register(displayName: String, login: String, password: String, onSuccess: () -> Unit) {
        if (login.isBlank() || password.isBlank()) {
            error.value = "Fill in login and password"
            info.value = null
            return
        }

        viewModelScope.launch {
            error.value = null
            info.value = null
            runCatching { authRepository.register(displayName, login, password) }
                .onSuccess { onSuccess() }
                .onFailure {
                    error.value = it.message ?: "Registration failed"
                }
        }
    }

    fun recoverPassword(login: String) {
        error.value = null
        info.value = if (login.isBlank()) {
            "Enter login first"
        } else {
            "Password recovery is not available in this backend"
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearMessages() {
        error.value = null
        info.value = null
    }
}
