package ru.netfantazii.handy.data.usecases.map

import android.content.Context
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import ru.netfantazii.handy.data.receiver.GEOFENCE_INTENT_ACTION
import ru.netfantazii.handy.data.localdb.GeofenceEntity
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.utils.GEOFENCE_CHECK_CYCLE_MILLIS
import ru.netfantazii.handy.utils.getPendingIntentForNotification
import javax.inject.Inject

@FragmentScope
class RegisterGeofencesUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    fun registerGeofences(
        geofenceEntitiesToAdd: List<GeofenceEntity>,
        catalogId: Long,
        catalogName: String,
        groupExpandState: RecyclerViewExpandableItemManager.SavedState
    ) = Completable.create { emitter ->

        if (geofenceEntitiesToAdd.isEmpty()) {
            emitter.onComplete()
        } else {

            val geofences = geofenceEntitiesToAdd.map {
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
                getPendingIntentForNotification(
                    context,
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
    }
}