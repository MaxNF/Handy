package ru.netfantazii.handy.core.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.db.GeofenceEntity
import kotlin.collections.Map

class MapViewModel(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long
) : ViewModel() {

    var circleMap = mapOf<Long, Circle>()
        private set(value) {
            field = value
            onNewDataReceive(value)
        }

    private val disposables = CompositeDisposable()

    private val _newDataReceived = MutableLiveData<Event<Unit>>()
    val newDataReceived: LiveData<Event<Unit>> = _newDataReceived

    init {
        subscribeToGeofencesChanges()
    }

    private fun onNewDataReceive(circleMap: Map<Long, Circle>) {
        _newDataReceived.value = Event(Unit)
    }

    private fun subscribeToGeofencesChanges() {
        disposables.add(localRepository.getGeofences(currentCatalogId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                circleMap = parseGeofencesToCircles(it)
            })
    }

    private fun parseGeofencesToCircles(geofences: List<GeofenceEntity>) =
        geofences.map {
            val point = Point(it.latitude, it.longitude)
            val circle = Circle(point, it.radius)
            it.id to circle
        }.toMap()

    fun onMapLongClick(point: Point) {
        val geofence = GeofenceEntity(catalogId = currentCatalogId,
            latitude = point.latitude,
            longitude = point.longitude, radius = currentRadius)
        localRepository.addGeofence(geofence)
    }

    fun onCircleClick(circleId: Long) {
        localRepository.removeGeofenceById(circleId)
    }

    fun onClearAllClick() {
        localRepository.removeAllGeofencesFromCatalog(currentCatalogId)
    }
}

class MapVmFactory(private val localRepository: LocalRepository, private val catalogId: Long) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(localRepository, catalogId) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}