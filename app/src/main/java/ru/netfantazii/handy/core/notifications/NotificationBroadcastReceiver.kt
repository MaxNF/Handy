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
import com.google.android.gms.location.GeofencingEvent
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import java.util.NoSuchElementException

const val ALARM_INTENT_ACTION = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val GEOFENCE_INTENT_ACTION = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"
const val CANCEL_NOTIFICATION_ACTION = "ru.netfantazii.handy.CANCEL_NOTIFICATION"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    private lateinit var context: Context
    private lateinit var onCancelClickIntent: PendingIntent
    private var notificationId = 0

    private var catalogId: Long = 0L
    private var catalogName: String? = null
    private var groupExpandState: RecyclerViewExpandableItemManager.SavedState? = null
    private val notificationIdKey = "notification_id"

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        if (intent.action == CANCEL_NOTIFICATION_ACTION) {
            val notificationId = intent.getIntExtra(notificationIdKey, -1)
            cancelNotification(notificationId)
        } else {

            val bundle = intent.extras!!.getBundle(BUNDLE_KEY)!!
            catalogId = bundle.getLong(BUNDLE_CATALOG_ID_KEY)
            catalogName = bundle.getString(BUNDLE_CATALOG_NAME_KEY)
            groupExpandState = bundle.getParcelable(BUNDLE_EXPAND_STATE_KEY)
            notificationId = catalogId.toInt()

            val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                action = CANCEL_NOTIFICATION_ACTION
                putExtra(notificationIdKey, notificationId)
            }
            onCancelClickIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0)

            when (intent.action) {
                ALARM_INTENT_ACTION -> sendAlarmNotification()
                GEOFENCE_INTENT_ACTION -> {
                    val geofencingEvent = GeofencingEvent.fromIntent(intent)
                    val geofenceIds =
                        geofencingEvent.triggeringGeofences.map { it.requestId.toLong() }
                            .toLongArray()
                    sendGeofenceNotification(geofenceIds)
                }
                else -> throw NoSuchElementException("Unknown intent action.")
            }
        }
    }

    private fun sendAlarmNotification() {
        Log.d(TAG, "sendAlarmNotification: ")
        val title = context.getString(R.string.alarm_went_off_title)
        val message = context.getString(R.string.geofence_notification_message)
        NotificationManagerCompat.from(context)
            .notify(notificationId, createAlarmNotification(title, message))
    }

    private fun sendGeofenceNotification(geofenceIds: LongArray) {
        Log.d(TAG, "sendGeofenceNotification: ")
        val title = context.getString(R.string.geofence_notification_title)
        val message = context.getString(R.string.geofence_notification_message)
        NotificationManagerCompat.from(context)
            .notify(notificationId, createGeofenceNotification(title, message, geofenceIds))
    }

    private fun createAlarmNotification(title: String, message: String): Notification {
        val arguments = Bundle()
        with(arguments) {
            putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
            putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
            putBoolean(BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, false)
            putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        }
        val toCatalogPendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.products_fragment)
            .setArguments(arguments)
            .createPendingIntent()

        val toSetAlarmPendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.notifications_fragment)
            .setArguments(arguments)
            .createPendingIntent()

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toCatalogPendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .addAction(0,
                context.getString(R.string.alarm_notification_action_label),
                toSetAlarmPendingIntent)
            .build()
    }

    private fun createGeofenceNotification(
        title: String,
        message: String,
        geofenceIds: LongArray
    ): Notification {
        val arguments = Bundle()
        with(arguments) {
            putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
            putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
            putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
            putBoolean(BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, true)
            putLongArray(BUNDLE_GEOFENCE_IDS_KEY, geofenceIds)
        }
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.products_fragment)
            .setArguments(arguments)
            .createPendingIntent()

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .build()
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}