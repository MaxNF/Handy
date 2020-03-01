package ru.netfantazii.handy.core.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingEvent
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.core.main.REMINDER_NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import ru.netfantazii.handy.extensions.getCancelPendingIntentForNotifications
import ru.netfantazii.handy.extensions.getNotificationSoundUri
import ru.netfantazii.handy.extensions.registerAlarm
import ru.netfantazii.handy.extensions.registerGeofences
import ru.netfantazii.handy.model.CatalogNotificationContent
import java.util.*
import java.util.concurrent.TimeUnit

const val ALARM_INTENT_ACTION = "ru.netfantazii.handy.ALARM_GOES_OFF"
const val GEOFENCE_INTENT_ACTION = "ru.netfantazii.handy.GEOFENCE_IS_CROSSED"
const val CANCEL_NOTIFICATION_ACTION = "ru.netfantazii.handy.CANCEL_NOTIFICATION"

const val BUNDLE_NOTIFICATION_ID_KEY = "notification_id"
const val BUNDLE_CATALOG_ID_KEY = "catalogId"
const val BUNDLE_CATALOG_NAME_KEY = "catalogName"
const val BUNDLE_EXPAND_STATE_KEY = "groupExpandStates"
const val BUNDLE_GEOFENCE_IDS_KEY = "geofenceIds"
const val BUNDLE_KEY = "bundle_key"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "NotificationBroadcastRe"
    private lateinit var context: Context
    private var notificationId = 0
    private var catalogId: Long = 0L
    private var catalogName: String? = null
    private var groupExpandState: RecyclerViewExpandableItemManager.SavedState? = null
    lateinit var localRepository: LocalRepository

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        Log.d(TAG, "onReceive: ${intent.action}")
        if (intent.action == CANCEL_NOTIFICATION_ACTION) {
            val notificationId = intent.getIntExtra(BUNDLE_NOTIFICATION_ID_KEY, -1)
            cancelNotification(notificationId)
            return
        }

        localRepository = (context.applicationContext as HandyApplication).localRepository

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            reregisterAllGeofencesAndAlarms(false)
            return
        }

        if (intent.action == Intent.ACTION_PACKAGE_DATA_CLEARED) {
            val uri = intent.data
            if (uri.toString() == "package:com.google.android.gms") {
                reregisterAllGeofencesAndAlarms(true)
                return
            }
        }

        val bundle = intent.extras!!.getBundle(BUNDLE_KEY)!!
        catalogId = bundle.getLong(BUNDLE_CATALOG_ID_KEY)
        catalogName = bundle.getString(BUNDLE_CATALOG_NAME_KEY)
        groupExpandState = bundle.getParcelable(BUNDLE_EXPAND_STATE_KEY)
        notificationId = catalogId.toInt()

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

        return NotificationCompat.Builder(context,
            REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shopping_cart)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, R.color.notificationColor))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductPendingIntent)
            .setSound(getNotificationSoundUri())
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                getCancelPendingIntentForNotifications(context, notificationId))
            .addAction(0,
                context.getString(R.string.alarm_notification_action_label),
                toAlarmPendingIntent)
            .build()
    }

    private fun alarmArguments() = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
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

        return NotificationCompat.Builder(context,
            REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shopping_cart)
            .setColor(ContextCompat.getColor(context, R.color.notificationColor))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(getNotificationSoundUri())
            .setAutoCancel(true)
            .setContentIntent(toProductPendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                getCancelPendingIntentForNotifications(context, notificationId))
            .build()
    }

    private fun geofenceArguments(geofenceIds: LongArray) = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        putLongArray(BUNDLE_GEOFENCE_IDS_KEY, geofenceIds)
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    @SuppressLint("CheckResult")
    private fun reregisterAllGeofencesAndAlarms(isFromGoogleServicesClearing: Boolean) {
        val pendingResult = goAsync()

        val observable = if (isFromGoogleServicesClearing) {
            // Если вызывать без задержки, сразу после удаления данных из служб Гугл Плей, то
            // вылетает исключение. Хз почему. Видимо какие-то процессы служб Гугл Плей
            // необходимые для работы геозон не успеают завершиться после удаления данных.
            localRepository.getAllGeofences().delay(5, TimeUnit.SECONDS)
        } else {
            localRepository.getAllGeofences()
        }

        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { geofences ->
                val geofencesByCatalogId = geofences.groupBy { it.catalogId }
                localRepository.getCatalogsSignleTime()
                    .subscribeOn(Schedulers.io())
                    .map { catalogs ->
                        val catalogNotificationContents = catalogs.map {
                            CatalogNotificationContent(it.id,
                                it.name,
                                it.groupExpandStates,
                                it.alarmTime,
                                geofencesByCatalogId[it.id] ?: listOf())
                        }
                        catalogNotificationContents
                    }
            }
            .flatMapCompletable { catalogNotificationContents ->
                catalogNotificationContents.forEach {
                    if (it.alarmTime != null) {
                        registerAlarm(context,
                            it.catalogId,
                            it.catalogName,
                            it.groupExpandStates,
                            it.alarmTime)
                    }
                }
                val completables = catalogNotificationContents.map {
                    registerGeofences(context,
                        it.geofenceEntities,
                        it.catalogId,
                        it.catalogName,
                        it.groupExpandStates)
                }
                Completable.mergeDelayError(completables)
            }.doAfterTerminate { pendingResult.finish() }
            .subscribe({}, {
                // ignore errors
            })
    }
}