package com.friendzone.android.location

import com.friendzone.android.data.LocationRepository
import com.friendzone.android.network.dto.LocationSampleDto
import com.friendzone.android.notifications.Notifier
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

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            locationProvider.locationUpdates().collectLatest { sample ->
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

    fun stop() {
        job?.cancel()
        job = null
    }
}
