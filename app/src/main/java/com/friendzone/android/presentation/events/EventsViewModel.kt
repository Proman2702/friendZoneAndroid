package com.friendzone.android.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendzone.android.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventUi(
    val type: String,
    val zoneName: String,
    val timeText: String
)

data class EventsState(
    val isLoading: Boolean = false,
    val events: List<EventUi> = emptyList()
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { repository.getEvents() }
                .onSuccess { events ->
                    _state.value = EventsState(
                        isLoading = false,
                        events = events.map {
                            EventUi(
                                type = it.type,
                                zoneName = it.zoneName,
                                timeText = it.eventTime ?: "No time"
                            )
                        }
                    )
                }
                .onFailure {
                    _state.value = EventsState(isLoading = false)
                }
        }
    }
}


