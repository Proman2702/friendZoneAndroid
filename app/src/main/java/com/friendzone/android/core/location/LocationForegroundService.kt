package com.friendzone.android.core.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.friendzone.android.R

class LocationForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FriendZone")
            .setContentText("Tracking location for active zones")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "friendzone_location"
        private const val NOTIFICATION_ID = 1001
    }
}


