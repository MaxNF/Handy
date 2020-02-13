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
import com.google.android.gms.location.GeofencingEvent
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import java.util.NoSuchElementException

const val ALARM_INTENT_ACTION = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val GEOFENCE_INTENT_ACTION = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"
const val CANCEL_NOTIFICATION_ACTION = "ru.netfantazii.handy.CANCEL_NOTIFICATION"

const val BUNDLE_NOTIFICATION_ID_KEY = "notification_id"
const val BUNDLE_CATALOG_ID_KEY = "catalogId"
const val BUNDLE_CATALOG_NAME_KEY = "catalogName"
const val BUNDLE_EXPAND_STATE_KEY = "groupExpandStates"
const val BUNDLE_GEOFENCE_IDS_KEY = "geofenceIds"
//todo проверить и убрать поле BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, если нигде больше не используется
const val BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY = "fromNotification"
const val BUNDLE_KEY = "bundle_key"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    private lateinit var context: Context
    private lateinit var onCancelClickIntent: PendingIntent
    private var notificationId = 0
    private var catalogId: Long = 0L
    private var catalogName: String? = null
    private var groupExpandState: RecyclerViewExpandableItemManager.SavedState? = null
    lateinit var localRepository: LocalRepository

    override fun onReceive(context: Context, intent: Intent) {

        this.context = context
        if (intent.action == CANCEL_NOTIFICATION_ACTION) {
            val notificationId = intent.getIntExtra(BUNDLE_NOTIFICATION_ID_KEY, -1)
            cancelNotification(notificationId)
        } else {
            localRepository = (context.applicationContext as HandyApplication).localRepository
            val bundle = intent.extras!!.getBundle(BUNDLE_KEY)!!
            catalogId = bundle.getLong(BUNDLE_CATALOG_ID_KEY)
            catalogName = bundle.getString(BUNDLE_CATALOG_NAME_KEY)
            groupExpandState = bundle.getParcelable(BUNDLE_EXPAND_STATE_KEY)
            notificationId = catalogId.toInt()

            val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                action = CANCEL_NOTIFICATION_ACTION
                putExtra(BUNDLE_NOTIFICATION_ID_KEY, notificationId)
            }
            onCancelClickIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0)

            when (intent.action) {
                ALARM_INTENT_ACTION -> {
                    removeAlarmFromDb()
                    sendAlarmNotification()
                }
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

    private fun removeAlarmFromDb() {
        localRepository.removeCatalogAlarmTime(catalogId)
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
        val toAlarmIntent = Intent(context, NotificationService::class.java).apply {
            action = ALARM_TO_ALARM_INTENT_ACTION
            putExtra(BUNDLE_KEY, alarmArguments())
        }
        val toProductsIntent = Intent(context, NotificationService::class.java).apply {
            action = ALARM_TO_PRODUCTS_INTENT_ACTION
            putExtra(BUNDLE_KEY, alarmArguments())
        }

        val toAlarmPendingIntent =
            PendingIntent.getService(context,
                notificationId,
                toAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val toProductPendingIntent =
            PendingIntent.getService(context,
                notificationId,
                toProductsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductPendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .addAction(0,
                context.getString(R.string.alarm_notification_action_label),
                toAlarmPendingIntent)
            .build()
    }

    private fun alarmArguments() = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
        putBoolean(BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, false)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        putInt(BUNDLE_NOTIFICATION_ID_KEY, notificationId)
    }

    private fun createGeofenceNotification(
        title: String,
        message: String,
        geofenceIds: LongArray
    ): Notification {
        val toProductIntent = Intent(context, NotificationService::class.java).apply {
            action = GEOFENCE_TO_PRODUCTS_INTENT_ACTION
            putExtra(BUNDLE_KEY, geofenceArguments(geofenceIds))
        }

        val toProductPendingIntent = PendingIntent.getService(context,
            notificationId,
            toProductIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductPendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .build()
    }

    private fun geofenceArguments(geofenceIds: LongArray) = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        putBoolean(BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, true)
        putLongArray(BUNDLE_GEOFENCE_IDS_KEY, geofenceIds)
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}