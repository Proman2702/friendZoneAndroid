package com.friendzone.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.AppPreferences
import com.friendzone.android.data.AuthRepository
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
    prefs: AppPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val error = MutableStateFlow<String?>(null)
    private val info = MutableStateFlow<String?>(null)

    val state: StateFlow<AuthUiState> = combine(prefs.isLoggedIn, error, info) { isLoggedIn, errorText, infoText ->
        AuthUiState(isLoggedIn, errorText, infoText)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthUiState())

    fun login(email: String, password: String) = runAuth(
        invalid = email.isBlank() || password.isBlank(),
        invalidMessage = "Заполните почту и пароль"
    ) {
        authRepository.login(email, password)
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) = runAuth(
        invalid = name.isBlank() || email.isBlank() || password.isBlank(),
        invalidMessage = "Заполните имя, почту и пароль",
        successMessage = "Аккаунт создан. Теперь войдите в него.",
        onSuccess = onSuccess
    ) {
        authRepository.register(name, email, password)
    }

    fun recoverPassword(email: String) {
        if (email.isBlank()) {
            error.value = "Введите почту"
            info.value = null
            return
        }
        viewModelScope.launch {
            runCatching { authRepository.recoverPassword(email) }
                .onSuccess {
                    error.value = null
                    info.value = it
                }
                .onFailure {
                    error.value = it.message ?: "Не удалось отправить запрос"
                    info.value = null
                }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearMessages() {
        error.value = null
        info.value = null
    }

    private fun runAuth(
        invalid: Boolean,
        invalidMessage: String,
        successMessage: String? = null,
        onSuccess: (() -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        if (invalid) {
            error.value = invalidMessage
            info.value = null
            return
        }
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess {
                    error.value = null
                    info.value = successMessage
                    onSuccess?.invoke()
                }
                .onFailure {
                    error.value = it.message ?: "Ошибка сети"
                    info.value = null
                }
        }
    }
}
