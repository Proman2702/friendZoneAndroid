package com.friendzone.android.presentation.bootstrap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.core.location.LocationUploader
import com.friendzone.android.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BootstrapState(
    val errorMessage: String? = null
)

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    private val app: Application,
    private val clientRepository: ClientRepository,
    private val locationUploader: LocationUploader
) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(BootstrapState())
    val state: StateFlow<BootstrapState> = _state

    fun ensureRegistered() {
        viewModelScope.launch {
            val result = clientRepository.ensureRegistered()
            result.onFailure { error ->
                _state.value = BootstrapState(
                    errorMessage = error.message ?: "Failed to reach backend"
                )
            }
            if (result.isSuccess) {
                _state.value = BootstrapState()
                locationUploader.start(viewModelScope)
            }
        }
    }
}


