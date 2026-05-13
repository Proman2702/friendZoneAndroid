package com.friendzone.android.core.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.friendzone.android.core.work.WorkScheduler

object LocationTrackingManager {
    fun start(context: Context, intervalMinutes: Double) {
        WorkScheduler.scheduleLocationUploads(context, intervalMinutes)
        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationForegroundService::class.java)
        )
    }

    fun restart(context: Context, intervalMinutes: Double) {
        context.stopService(Intent(context, LocationForegroundService::class.java))
        start(context, intervalMinutes)
    }
}
