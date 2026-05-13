package com.friendzone.android.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    fun scheduleLocationUploads(context: Context, intervalMinutes: Double) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<LocationUploadWorker>(
            kotlin.math.ceil(intervalMinutes.coerceAtLeast(15.0)).toLong(),
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "friendzone_location_upload",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}


