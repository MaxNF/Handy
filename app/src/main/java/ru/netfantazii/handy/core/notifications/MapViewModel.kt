package ru.netfantazii.handy.core.notifications

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
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


    private val TAG = "MapViewModel"

    var circleMap = mapOf<Long, Circle>()
        private set(value) {
            field = value
            onNewDataReceive(value)
        }

    var lastCameraPosition: CameraPosition? = null
    var lastPinPosition: Point? = null
    private val minSeekBarValue: Int = 100
    var seekBarValue: Int = 0
        set(value) {
            field = value
            refreshSeekBarValueField(field)
        }

    val seekBarDisplayValue: ObservableField<String> =
        ObservableField(minSeekBarValue.toString())

    val nextGeofenceRaidus: Float
        get() = (seekBarValue + minSeekBarValue).toFloat()

    private val disposables = CompositeDisposable()

    private val _newDataReceived = MutableLiveData<Event<Unit>>()
    val newDataReceived: LiveData<Event<Unit>> = _newDataReceived

    private val _findMyLocationClicked = MutableLiveData<Event<Unit>>()
    val findMyLocationClicked: LiveData<Event<Unit>> = _findMyLocationClicked

    private val _applyClicked = MutableLiveData<Event<Unit>>()
    val applyClicked: LiveData<Event<Unit>> = _applyClicked

    init {
        subscribeToGeofencesChanges()
    }

    private fun refreshSeekBarValueField(value: Int) {
        seekBarDisplayValue.set((value + minSeekBarValue).toString())
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
            longitude = point.longitude, radius = nextGeofenceRaidus)
        localRepository.addGeofence(geofence)
    }

    fun onCircleClick(circleId: Long) {
        localRepository.removeGeofenceById(circleId)
    }

    fun onClearAllClick() {
        localRepository.removeAllGeofencesFromCatalog(currentCatalogId)
    }

    fun onFindMyLocationClick() {
        _findMyLocationClicked.value = Event(Unit)
    }

    fun onApplyClick() {
        _applyClicked.value = Event(Unit)
        Log.d(TAG, "onApplyClick: $nextGeofenceRaidus")
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