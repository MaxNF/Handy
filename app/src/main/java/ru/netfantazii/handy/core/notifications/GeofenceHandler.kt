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

class GeofenceHandler(private val context: Context, catalogId: Long) {
    val geofenceCheckCycleMillis = 30000
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = geofenceIntentAction
        PendingIntent.getBroadcast(context,
            catalogId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
    val geofencingClient = LocationServices.getGeofencingClient(context)

    fun registerGeofence(geofenceEntity: GeofenceEntity) {
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

        geofencingClient.addGeofences(getGeofencingRequest(geofence),
            geofencePendingIntent).apply {
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

    fun unregisterGeofence(geofenceEntity: GeofenceEntity) {
        val geofenceRequestId = listOf(geofenceEntity.id.toString())
        geofencingClient.removeGeofences(geofenceRequestId).apply {
            addOnSuccessListener {
                Toast.makeText(context,
                    context.getString(R.string.geofence_unreg_success),
                    Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener { throw it }
        }
    }

    fun unregisterAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent).apply {
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