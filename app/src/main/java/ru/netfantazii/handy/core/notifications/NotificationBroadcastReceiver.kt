package ru.netfantazii.handy.core.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import java.util.NoSuchElementException

const val alarmIntentAction = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val geofenceIntentAction = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    private lateinit var context: Context
    private lateinit var onNotificationClickIntent: PendingIntent
    private var notificationId = 0

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: broadcast received")
        this.context = context

        val bundle = intent.extras!!.getBundle(BUNDLE_KEY)!!

        val catalogId = bundle.getLong(BUNDLE_CATALOG_ID_KEY)
        val catalogName = bundle.getString(BUNDLE_CATALOG_NAME_KEY)
        val groupExpandState: RecyclerViewExpandableItemManager.SavedState? =
            bundle.getParcelable(BUNDLE_EXPAND_STATE_KEY)

        Log.d(TAG, "onReceive: $catalogId, $catalogName")

        notificationId = catalogId.toInt()

        val arguments = Bundle()
        with(arguments) {
            putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
            putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
            putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        }

        onNotificationClickIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.products_fragment)
            .setArguments(arguments)
            .createPendingIntent()

        when (intent.action) {
            alarmIntentAction -> sendAlarmNotification()
            geofenceIntentAction -> sendGeofenceNotification()
            else -> throw NoSuchElementException("Unknown intent action.")
        }
    }

    private fun sendAlarmNotification() {
        Log.d(TAG, "sendAlarmNotification: ")
    }

    private fun sendGeofenceNotification() {
        Log.d(TAG, "sendGeofenceNotification: ")
        val title = context.getString(R.string.geofence_notification_title)
        val message = context.getString(R.string.geofence_notification_message)
        NotificationManagerCompat.from(context)
            .notify(notificationId, createNotification(title, message))
    }

    private fun createNotification(title: String, message: String): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(onNotificationClickIntent)
            .build()
    }
}