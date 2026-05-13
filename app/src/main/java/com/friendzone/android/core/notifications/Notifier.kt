package com.friendzone.android.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.friendzone.android.R

class Notifier(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun showEventNotification(title: String, body: String) {
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel() {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "События зон",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setSound(alarmSound, attributes)
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 300, 200, 300)
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "friendzone_events"
    }
}


