package ru.netfantazii.handy.core.notifications.map

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.db.GeofenceEntity
import ru.netfantazii.handy.extensions.registerGeofence
import ru.netfantazii.handy.extensions.unregisterAllGeofences
import ru.netfantazii.handy.extensions.unregisterGeofence
import kotlin.collections.Map

class MapViewModel(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long,
    private val catalogName: String,
    private val groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
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
    private val seekBarOneSegment = 50
    var seekBarValue: Int = 0
        set(value) {
            field = value * seekBarOneSegment
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
        Log.d(TAG, "onMapLongClick: $nextGeofenceRaidus")
        val geofence = GeofenceEntity(catalogId = currentCatalogId,
            latitude = point.latitude,
            longitude = point.longitude, radius = nextGeofenceRaidus)
        disposables.add(localRepository.addGeofence(geofence).subscribe(Consumer {
            geofence.id = it
            registerGeofence(getApplication(),
                geofence,
                currentCatalogId,
                catalogName,
                groupExpandStates) {
                val context = getApplication<HandyApplication>()
                Toast.makeText(context,
                    context.getString(R.string.geofence_success),
                    Toast.LENGTH_SHORT).show()
            }
        }))

    }

    fun onCircleClick(geofenceId: Long) {
        localRepository.removeGeofenceById(geofenceId)
        unregisterGeofence(getApplication(), geofenceId) {
            val context = getApplication<HandyApplication>()
            Toast.makeText(context,
                context.getString(R.string.geofence_unreg_success),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun onClearAllClick() {
        localRepository.removeAllGeofencesFromCatalog(currentCatalogId)
        unregisterAllGeofences(getApplication(), currentCatalogId)
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
    private val catalogName: String,
    private val groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    private val application: Application
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(
                localRepository,
                catalogId,
                catalogName,
                groupExpandStates,
                application) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}