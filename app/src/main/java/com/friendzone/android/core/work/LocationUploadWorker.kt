package com.friendzone.android.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.friendzone.android.data.repository.LocationRepository
import com.friendzone.android.core.location.LocationProvider
import com.friendzone.android.data.remote.dto.LocationSampleDto
import com.friendzone.android.core.notifications.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@HiltWorker
class LocationUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationProvider: LocationProvider,
    private val locationRepository: LocationRepository,
    private val notifier: Notifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sample = withTimeoutOrNull(8_000L) { locationProvider.locationUpdates().first() }
            ?: return Result.retry()

        val events = locationRepository.sendBatch(
            listOf(
                LocationSampleDto(
                    lat = sample.lat,
                    lon = sample.lon,
                    accuracy = sample.accuracy,
                    deviceTime = sample.deviceTimeIso
                )
            )
        )

        events.forEach { event ->
            val title = "${event.type} ${event.zoneName}"
            val body = event.eventTime ?: "Zone event"
            notifier.showEventNotification(title, body)
        }

        return Result.success()
    }
}


