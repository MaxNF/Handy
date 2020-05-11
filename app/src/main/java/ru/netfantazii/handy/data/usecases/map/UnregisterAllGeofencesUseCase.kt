package ru.netfantazii.handy.data.usecases.map

import android.content.Context
import com.google.android.gms.location.LocationServices
import ru.netfantazii.handy.data.receiver.GEOFENCE_INTENT_ACTION
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.utils.getPendingIntentForCancel
import javax.inject.Inject

@FragmentScope
class UnregisterAllGeofencesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    fun unregisterAllGeofences(
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
}