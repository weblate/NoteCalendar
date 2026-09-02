package com.sztorm.notecalendar

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NoteNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtraCompat(
            IntentKeys.NOTIFICATION_EXTRA, Notification::class.java
        )
        val noteDateId = intent
            .getIntExtra(IntentKeys.NOTE_DATE_ID, 0)

        if (notification != null) {
            manager.notify(noteDateId, notification)
        }
    }
}