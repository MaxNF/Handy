package ru.netfantazii.handy.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.core.Event

class GeofenceHandler(private val applicationContext: Context, private val catalogId: Long) {
    val localRepository: LocalRepository = (applicationContext as HandyApplication).localRepository
    val geofenceCheckCycleMillis = 30000
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun registerGeofence() {
        localRepository.getGeofences(catalogId).observeOn(Schedulers.newThread())
            .subscribe { geofenceList ->
                val readyGeofences = mutableListOf<Geofence>()
                geofenceList.mapTo(readyGeofences) {
                    Geofence.Builder()
                        .setRequestId(it.id.toString())
                        .setCircularRegion(it.latitude, it.longitude, it.radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setNotificationResponsiveness(geofenceCheckCycleMillis)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()
                }


                val geofencingClient = LocationServices.getGeofencingClient(applicationContext)
                geofencingClient.addGeofences(getGeofencingRequest(readyGeofences),
                    geofencePendingIntent).apply {
                    addOnSuccessListener {
                        Toast.makeText(applicationContext, "GEOFENCE ADDED", Toast.LENGTH_SHORT).show()
                    }
                    addOnFailureListener {
                        Toast.makeText(applicationContext, "GEOFENCE NOT ADDED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun unregisterGeofence() {

    }

    private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            addGeofences(geofenceList)
        }.build()
    }
}