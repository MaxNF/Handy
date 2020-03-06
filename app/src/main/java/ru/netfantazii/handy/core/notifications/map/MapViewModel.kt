package ru.netfantazii.handy.core.notifications.map

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.extensions.*
import ru.netfantazii.handy.data.database.GeofenceEntity
import ru.netfantazii.handy.data.GeofenceLimitException
import kotlin.collections.Map

class MapViewModel(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long,
    private val catalogName: String,
    private val groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    application: Application
) : AndroidViewModel(application) {

    private var circleFillColor: Int = ContextCompat.getColor(application, R.color.circleFillColor)
    private var circleStrokeColor: Int =
        ContextCompat.getColor(application, R.color.circleStrokeColor)
    private var circleStrokeWidth: Float = 3.0f

    private val TAG = "MapViewModel"

    var circleMap = mapOf<Long, CircleOptions>()
        private set(value) {
            field = value
            onNewDataReceive(value)
        }

    var searchValue = ""
        set(value) {
            field = value
            onNewSearchValueReceive(value)
        }

    //    var lastSearchPoints: List<Point?>? = null
//    var lastCameraPosition: CameraPosition? = null
//    var lastPinPosition: Point? = null
    val searchMarkersList = mutableListOf<Marker>()
    val geofenceIconList = mutableListOf<Marker>()
    val circleList = mutableListOf<Circle>()

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

    private val _newDataReceived = MutableLiveData<Event<Map<Long, CircleOptions>>>()
    val newDataReceived: LiveData<Event<Map<Long, CircleOptions>>> = _newDataReceived

    private val _findMyLocationClicked = MutableLiveData<Event<Unit>>()
    val findMyLocationClicked: LiveData<Event<Unit>> = _findMyLocationClicked

    private val _applyClicked = MutableLiveData<Event<Unit>>()
    val applyClicked: LiveData<Event<Unit>> = _applyClicked

    private val _newSearchValueReceived = MutableLiveData<Event<String>>()
    val newSearchValueReceived: LiveData<Event<String>> = _newSearchValueReceived

    private val _geofenceLimitForFreeVersionReached = MutableLiveData<Event<Unit>>()
    val geofenceLimitForFreeVersionReached: LiveData<Event<Unit>> =
        _geofenceLimitForFreeVersionReached

    init {
        subscribeToGeofencesChanges()
    }

    private fun refreshSeekBarValueField(value: Int) {
        seekBarDisplayValue.set((value + minSeekBarValue).toString())
    }

    private fun onNewDataReceive(circleMap: Map<Long, CircleOptions>) {
        _newDataReceived.value = Event(circleMap)
    }

    private fun onNewSearchValueReceive(string: String) {
        Log.d(TAG, "onNewSearchValueReceive: ")
        _newSearchValueReceived.value = Event(string)
    }

    private fun subscribeToGeofencesChanges() {
        disposables.add(localRepository.getGeofences(currentCatalogId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                circleMap = parseGeofencesToCirclesOptions(it)
            })
    }

    private fun parseGeofencesToCirclesOptions(geofences: List<GeofenceEntity>) =
        geofences.map {
            val circleOptions = CircleOptions()
                .center(LatLng(it.latitude, it.longitude))
                .radius(it.radius.toDouble())
                .fillColor(circleFillColor)
                .strokeColor(circleStrokeColor)
                .strokeWidth(circleStrokeWidth)
                .clickable(true)
            it.id to circleOptions
        }.toMap()

    fun onMapLongClick(latitude: Double, longitude: Double) {
        Log.d(TAG, "onMapLongClick: $nextGeofenceRaidus")
        val geofence =
            GeofenceEntity(catalogId = currentCatalogId,
                latitude = latitude,
                longitude = longitude, radius = nextGeofenceRaidus)
        disposables.add(
            // получаем общее кол-во геозон и кидает искл. если больше допустимого лимита
            localRepository.getTotalGeofenceCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { geofenceCount ->
                    if (geofenceCount >= GEOFENCE_APP_LIMIT) {
                        throw GeofenceLimitException()
                    } else {
                        // если все ок, добавляем геозону в бд и получаем ИД, передаем ИД и кол-во дальше
                        localRepository.addGeofence(geofence)
                            .subscribeOn(Schedulers.io())
                            .map { geofenceId ->
                                Pair(geofenceId, geofenceCount)
                            }
                    }
                }
                // пытаемся зарегистрировать геозону, если ошибка, то удаляем геозону из бд
                .flatMap { (geofenceId, geofenceCount) ->
                    geofence.id = geofenceId
                    registerGeofences(getApplication(),
                        listOf(geofence),
                        currentCatalogId,
                        catalogName,
                        groupExpandStates)
                        .subscribeOn(Schedulers.io())
                        .doOnError {
                            localRepository.removeGeofenceById(geofenceId)
                        }.toSingle {
                            geofenceCount
                        }
                }.subscribe({ geofenceCount ->
                    // если все прошло успешно, то показываем тост с оставшимся лимитом геозон
                    val context = getApplication<HandyApplication>()
                    Toast.makeText(context,
                        context.getString(R.string.geofence_success,
                            GEOFENCE_APP_LIMIT - (geofenceCount + 1),
                            GEOFENCE_APP_LIMIT),
                        Toast.LENGTH_SHORT).show()
                }, {
                    // если была ошибка то либо выводим тост ошибкой и с советом проверить включен
                    // ли gps, либо (если ошибка неизвестна) просто уведомляем пользователя об ошибке
                    when {
                        it is GeofenceLimitException -> {
                            _geofenceLimitForFreeVersionReached.value = Event(Unit)
                        }
                        it is ApiException && it.message == GEOFENCE_UNAVAILABLE_ERROR_MESSAGE -> {
                            showLongToast(getApplication(),
                                getApplication<Application>().getString(R.string.geofence_api_error))
                        }
                        else -> {
                            showLongToast(getApplication(),
                                getApplication<Application>().getString(R.string.adding_geofence_failed))
                        }
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
        unregisterAllGeofences(getApplication(), currentCatalogId) { context ->
            Toast.makeText(context,
                context.getString(R.string.all_geofences_unreg_success),
                Toast.LENGTH_SHORT).show()
        }
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