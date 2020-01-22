package ru.netfantazii.handy.core.notifications

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.*
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
    private val currentCatalogId: Long,
    private val geofenceHandler: GeofenceHandler,
    application: Application
) : AndroidViewModel(application) {


    private val TAG = "MapViewModel"

    var circleMap = mapOf<Long, Circle>()
        private set(value) {
            field = value
            onNewDataReceive(value)
        }

    var searchValue = ""
        set(value) {
            field = value
            onNewSearchValueReceive(value)
        }

    var lastSearchPoints: List<Point?>? = null
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

    private val _newSearchValueReceived = MutableLiveData<Event<String>>()
    val newSearchValueReceived: LiveData<Event<String>> = _newSearchValueReceived

    init {
        subscribeToGeofencesChanges()
    }

    private fun refreshSeekBarValueField(value: Int) {
        seekBarDisplayValue.set((value + minSeekBarValue).toString())
    }

    private fun onNewDataReceive(circleMap: Map<Long, Circle>) {
        _newDataReceived.value = Event(Unit)
    }

    private fun onNewSearchValueReceive(string: String) {
        Log.d(TAG, "onNewSearchValueReceive: ")
        _newSearchValueReceived.value = Event(string)
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
        geofenceHandler.registerGeofence(getApplication(), geofence)
    }

    fun onCircleClick(geofenceId: Long) {
        localRepository.removeGeofenceById(geofenceId)
        geofenceHandler.unregisterGeofence(getApplication(), geofenceId)
    }

    fun onClearAllClick() {
        localRepository.removeAllGeofencesFromCatalog(currentCatalogId)
        geofenceHandler.unregisterAllGeofences(getApplication())
    }

    fun onFindMyLocationClick() {
        _findMyLocationClicked.value = Event(Unit)
    }

    fun onApplyClick() {
        _applyClicked.value = Event(Unit)
        Log.d(TAG, "onApplyClick: $nextGeofenceRaidus")
    }

//    fun onSearchButtonClick() {
//        _newSearchValueReceived.value = Event(searchValue)
//    }
}

class MapVmFactory(
    private val localRepository: LocalRepository,
    private val catalogId: Long,
    private val geofenceHandler: GeofenceHandler,
    private val application: Application
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(localRepository, catalogId, geofenceHandler, application) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}