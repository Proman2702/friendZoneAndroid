package com.friendzone.android.core.location

import android.content.Context
import android.content.Intent
import com.friendzone.android.core.work.WorkScheduler

object LocationTrackingManager {
    fun start(context: Context, intervalMinutes: Double) {
        WorkScheduler.scheduleLocationUploads(context, intervalMinutes)
    }

    fun restart(context: Context, intervalMinutes: Double) {
        runCatching {
            context.stopService(Intent(context, LocationForegroundService::class.java))
        }
        start(context, intervalMinutes)
    }
}
