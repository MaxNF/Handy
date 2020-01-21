package ru.netfantazii.handy.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import java.lang.IllegalStateException
import java.util.NoSuchElementException

const val alarmIntentAction = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val geofenceIntentAction = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: broadcast received")
        val catalogId = intent.getLongExtra(INTENT_CATALOG_ID_KEY, -1)
        if (catalogId == -1L) throw NoSuchElementException("Wrong catalogId.")
        when (intent.action) {
            alarmIntentAction -> sendAlarmNotification(catalogId, context)
            geofenceIntentAction -> sendGeofenceNotification(catalogId, context)
            else -> throw NoSuchElementException("Unknown intent action.")
        }
    }

    private fun sendAlarmNotification(catalogId: Long, context: Context) {
        Log.d(TAG, "sendAlarmNotification: ")
    }

    private fun sendGeofenceNotification(catalogId: Long, context: Context) {
        Log.d(TAG, "sendGeofenceNotification: ")
        val title = context.getString(R.string.geofence_notification_title)
        val message = context.getString(R.string.geofence_notification_message)
        NotificationManagerCompat.from(context)
            .notify(catalogId.toInt(), createNotification(title, message, context))
    }

    private fun createNotification(title: String, message: String, context: Context) =
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()
}