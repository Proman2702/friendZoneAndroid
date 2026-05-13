package com.friendzone.android.core.location

import com.friendzone.android.data.repository.LocationRepository
import com.friendzone.android.data.remote.dto.LocationSampleDto
import com.friendzone.android.core.notifications.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationUploader(
    private val locationProvider: LocationProvider,
    private val repository: LocationRepository,
    private val notifier: Notifier
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope, intervalMillis: Long) {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            runCatching {
                locationProvider.locationUpdates(intervalMillis).collectLatest { sample ->
                    runCatching {
                        repository.sendBatch(
                            listOf(
                                LocationSampleDto(
                                    lat = sample.lat,
                                    lon = sample.lon,
                                    accuracy = sample.accuracy,
                                    deviceTime = sample.deviceTimeIso
                                )
                            )
                        )
                    }.onSuccess { events ->
                        events.forEach { event ->
                            val title = "${event.type} ${event.zoneName}"
                            val body = event.eventTime ?: "Zone event"
                            notifier.showEventNotification(title, body)
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}


