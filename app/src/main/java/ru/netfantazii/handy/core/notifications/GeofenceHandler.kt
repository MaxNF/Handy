package ru.netfantazii.handy.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.db.GeofenceEntity

const val BUNDLE_CATALOG_ID_KEY = "catalogId"
const val BUNDLE_CATALOG_NAME_KEY = "catalogName"
const val BUNDLE_EXPAND_STATE_KEY = "groupExpandStates"
const val BUNDLE_GEOFENCE_IDS_KEY = "geofenceIds"
const val BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY = "fromNotification"
const val BUNDLE_KEY = "bundle_key"

class GeofenceHandler(
    private val catalogId: Long,
    private val catalogName: String,
    private val groupExpandState: RecyclerViewExpandableItemManager.SavedState
) {
    private val TAG = "GeofenceHandler"
    private val geofenceCheckCycleMillis = 30000

    fun registerGeofence(context: Context, geofenceEntity: GeofenceEntity) {
        Log.d(TAG, "registerGeofence: ${geofenceEntity.id}")
        val geofence =
            Geofence.Builder()
                .setRequestId(geofenceEntity.id.toString())
                .setCircularRegion(geofenceEntity.latitude,
                    geofenceEntity.longitude,
                    geofenceEntity.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(geofenceCheckCycleMillis)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        LocationServices.getGeofencingClient(context).addGeofences(getGeofencingRequest(geofence),
            getPendingIntentForNotification(context,
                catalogId,
                catalogName,
                groupExpandState,
                GEOFENCE_INTENT_ACTION))
            .apply {
                addOnSuccessListener {
                    Toast.makeText(context,
                        context.getString(R.string.geofence_success),
                        Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    throw it
                }
            }
    }

    fun unregisterGeofence(context: Context, geofenceId: Long) {
        val geofenceRequestId = listOf(geofenceId.toString())
        LocationServices.getGeofencingClient(context).removeGeofences(geofenceRequestId).apply {
            addOnSuccessListener {
                Toast.makeText(context,
                    context.getString(R.string.geofence_unreg_success),
                    Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener { throw it }
        }
    }

    fun unregisterAllGeofences(context: Context) {
        LocationServices.getGeofencingClient(context)
            .removeGeofences(getPendingIntentForNotification(context,
                catalogId,
                catalogName,
                groupExpandState,
                GEOFENCE_INTENT_ACTION))
            .apply {
                addOnSuccessListener {
                    Toast.makeText(context,
                        context.getString(R.string.all_geofences_unreg_success),
                        Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    throw it
                }
            }
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            addGeofence(geofence)
        }.build()
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