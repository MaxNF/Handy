package ru.netfantazii.handy.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.*
import ru.netfantazii.handy.model.GeofenceListEmptyException
import ru.netfantazii.handy.model.database.GeofenceEntity
import java.util.*

const val GEOFENCE_CHECK_CYCLE_MILLIS = 30000
const val GEOFENCE_APP_LIMIT = 100
const val GEOFENCE_UNAVAILABLE_ERROR_MESSAGE =
    "1000: " // код ошибки прилетает с двоеточием и пробелом

private val TAG = "GeofenceHandler"


fun registerGeofences(
    context: Context,
    geofenceEntitiesToAdd: List<GeofenceEntity>,
    catalogId: Long,
    catalogName: String,
    groupExpandState: RecyclerViewExpandableItemManager.SavedState
) = Completable.create { emitter ->

    if (geofenceEntitiesToAdd.isEmpty()) {
        emitter.onError(GeofenceListEmptyException())
    }

    val geofences = geofenceEntitiesToAdd.map {
        Log.d(TAG, "registerGeofence: ${it.id}")
        Geofence.Builder()
            .setRequestId(it.id.toString())
            .setCircularRegion(it.latitude,
                it.longitude,
                it.radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setNotificationResponsiveness(GEOFENCE_CHECK_CYCLE_MILLIS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    val geofencingRequest = GeofencingRequest.Builder().apply {
        addGeofences(geofences)
    }.build()

    LocationServices.getGeofencingClient(context).addGeofences(geofencingRequest,
        getPendingIntentForNotification(context,
            catalogId,
            catalogName,
            groupExpandState,
            GEOFENCE_INTENT_ACTION))
        .apply {
            addOnSuccessListener {
                emitter.onComplete()
            }
            addOnFailureListener {
                emitter.onError(it)
            }
        }
}

fun unregisterGeofence(context: Context, geofenceId: Long, onSuccessAction: (() -> Unit)?) {
    val geofenceRequestId = listOf(geofenceId.toString())
    LocationServices.getGeofencingClient(context).removeGeofences(geofenceRequestId).apply {
        addOnSuccessListener {
            onSuccessAction?.invoke()
        }
        addOnFailureListener { throw it }
    }
}

fun unregisterAllGeofences(
    context: Context,
    catalogId: Long,
    onSuccessAction: ((context: Context) -> Unit)?
) {
    LocationServices.getGeofencingClient(context)
        .removeGeofences(getPendingIntentForCancel(
            context,
            catalogId,
            GEOFENCE_INTENT_ACTION))
        .apply {
            addOnSuccessListener {
                onSuccessAction?.invoke(context)
            }
            addOnFailureListener {
                throw it
            }
        }
}

fun getPendingIntentForNotification(
    context: Context,
    catalogId: Long,
    catalogName: String,
    groupExpandState: RecyclerViewExpandableItemManager.SavedState,
    action: String
): PendingIntent {
    val intent = Intent(context, NotificationBroadcastReceiver::class.java)
    intent.action = action
    val bundle = Bundle()
    bundle.putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
    bundle.putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
    bundle.putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
    intent.putExtra(BUNDLE_KEY, bundle)
    return PendingIntent.getBroadcast(context,
        catalogId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT)
}

fun getPendingIntentForCancel(
    context: Context,
    catalogId: Long, action: String
): PendingIntent {
    val intent = Intent(context, NotificationBroadcastReceiver::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(context,
        catalogId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT)
}

fun registerAlarm(
    context: Context,
    catalogId: Long,
    catalogName: String,
    expandStates: RecyclerViewExpandableItemManager.SavedState,
    triggerTime: Calendar
) {
    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent =
        getPendingIntentForNotification(
            context,
            catalogId,
            catalogName,
            expandStates,
            ALARM_INTENT_ACTION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            triggerTime.timeInMillis,
            pendingIntent)
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
    }
}

fun unregisterAlarm(
    context: Context,
    catalogId: Long
) {
    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent =
        getPendingIntentForCancel(
            context,
            catalogId,
            ALARM_INTENT_ACTION)
    alarmManager.cancel(pendingIntent)
}