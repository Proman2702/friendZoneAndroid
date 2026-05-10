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

    val state: StateFlow<AuthUiState> = combine(prefs.isLoggedIn, error, info) { isLoggedIn, errorText, infoText ->
        AuthUiState(isLoggedIn, errorText, infoText)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthUiState())

    @Suppress("UNUSED_PARAMETER")
    fun login(email: String, password: String) {
        viewModelScope.launch {
            error.value = null
            info.value = null
            prefs.saveUser(
                name = email.ifBlank { "Гость" },
                email = email.ifBlank { "guest@friendzone.local" }
            )
            prefs.setClientId("stub-client")
            prefs.setLoggedIn(true)

            // Временная заглушка: вход пропускает пользователя сразу в приложение.
            // Параметр password пока не используется.
            // authRepository.login(email, password)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            error.value = null
            info.value = null
            prefs.saveUser(
                name = name.ifBlank { "Гость" },
                email = email.ifBlank { "guest@friendzone.local" }
            )
            prefs.setClientId("stub-client")
            prefs.setLoggedIn(true)
            onSuccess()

            // Временная заглушка: реальная регистрация на сервер не выполняется.
            // Параметр password пока не используется.
            // authRepository.register(name, email, password)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun recoverPassword(email: String) {
        error.value = null
        info.value = "Восстановление пароля временно отключено"

        // Временная заглушка: запрос на сервер не отправляется.
        // if (email.isBlank()) {
        //     error.value = "Введите почту"
        //     info.value = null
        //     return
        // }
        // viewModelScope.launch {
        //     runCatching { authRepository.recoverPassword(email) }
        //         .onSuccess {
        //             error.value = null
        //             info.value = it
        //         }
        //         .onFailure {
        //             error.value = it.message ?: "Не удалось отправить запрос"
        //             info.value = null
        //         }
        // }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearMessages() {
        error.value = null
        info.value = null
    }
}
