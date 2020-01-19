package ru.netfantazii.handy.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.NoSuchElementException

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val alarmIntentAction = "ru.netfantazii.handy.ALARM_GOES_OFF"
    private val geofenceIntentAction = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"
    private val TAG = "NotificationBroadcastRe"
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

}