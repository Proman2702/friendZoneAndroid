package com.friendzone.android.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifier = Notifier(context)
        val title = intent.getStringExtra("title") ?: "FriendZone"
        val body = intent.getStringExtra("body") ?: "Событие зоны"
        notifier.showEventNotification(title, body)
    }
}


