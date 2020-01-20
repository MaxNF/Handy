package ru.netfantazii.handy.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import ru.netfantazii.handy.R
import java.util.NoSuchElementException

const val alarmIntentAction = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val geofenceIntentAction = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    private val channelId = "Handy notification channel"
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d(TAG, "onReceive: broadcast received")
        when (intent.action) {
            alarmIntentAction -> sendAlarmNotification(intent)
            geofenceIntentAction -> sendGeofenceNotification(intent)
            else -> throw NoSuchElementException("Unknown intent action.")
        }
    }

    private fun sendAlarmNotification(intent: Intent) {
        Log.d(TAG, "sendAlarmNotification: ")
    }

    private fun sendGeofenceNotification(intent: Intent) {
        Log.d(TAG, "sendGeofenceNotification: ")
    }

    private fun createNotification(title: String, message: String, context: Context) =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

    private fun registerNotitificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}