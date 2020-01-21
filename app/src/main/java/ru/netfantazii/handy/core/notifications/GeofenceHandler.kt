package ru.netfantazii.handy.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import ru.netfantazii.handy.R
import ru.netfantazii.handy.db.GeofenceEntity

const val INTENT_CATALOG_ID_KEY = "intent_catalog_id_key"

class GeofenceHandler(private val catalogId: Long) {
    val geofenceCheckCycleMillis = 30000

    fun registerGeofence(context: Context, geofenceEntity: GeofenceEntity) {
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
            getPendingIntent(context)).apply {
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
        LocationServices.getGeofencingClient(context).removeGeofences(getPendingIntent(context))
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

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = geofenceIntentAction
        intent.putExtra(INTENT_CATALOG_ID_KEY, catalogId)
        return PendingIntent.getBroadcast(context,
            catalogId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
}