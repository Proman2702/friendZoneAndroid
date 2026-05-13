package com.friendzone.android.core.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.friendzone.android.R
import com.friendzone.android.data.local.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationForegroundService : Service() {
    @Inject lateinit var locationUploader: LocationUploader
    @Inject lateinit var prefs: AppPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            val intervalMillis = (prefs.locationUpdateIntervalMinutes.first().coerceAtLeast(0.1) * 60_000L).toLong()
            locationUploader.start(serviceScope, intervalMillis)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationUploader.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Отслеживание геолокации",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FriendZone")
            .setContentText("Отслеживание геолокации для активных зон")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "friendzone_location"
        private const val NOTIFICATION_ID = 1001
    }
}


