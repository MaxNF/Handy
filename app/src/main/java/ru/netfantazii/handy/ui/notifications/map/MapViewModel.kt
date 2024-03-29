package ru.netfantazii.handy.ui.notifications.map

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.google.android.gms.common.api.ApiException
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.model.Event
import ru.netfantazii.handy.utils.extensions.*
import ru.netfantazii.handy.data.localdb.GeofenceEntity
import ru.netfantazii.handy.data.model.GeofenceLimitException
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.utils.GEOFENCE_UNAVAILABLE_ERROR_MESSAGE
import ru.netfantazii.handy.utils.registerGeofences
import ru.netfantazii.handy.utils.unregisterAllGeofences
import ru.netfantazii.handy.utils.unregisterGeofence
import javax.inject.Inject
import kotlin.collections.Map

class MapViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    @CatalogId private val currentCatalogId: Long,
    @CatalogName private val catalogName: String,
    private val groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val geofenceAppLimit
        get() = if ((context as HandyApplication).isPremium.get()) 100 else 1
    private val TAG = "MapViewModel"

    var circleMap = mapOf<Long, Circle>()
        private set(value) {
            field = value
            onNewDataReceive(value)
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

    private val _zoomInClicked = MutableLiveData<Event<Unit>>()
    val zoomInClicked: LiveData<Event<Unit>> = _zoomInClicked

    private val _zoomOutClicked = MutableLiveData<Event<Unit>>()
    val zoomOutClicked: LiveData<Event<Unit>> = _zoomOutClicked

    private val _geofenceLimitForFreeVersionReached = MutableLiveData<Event<Unit>>()
    val geofenceLimitForFreeVersionReached: LiveData<Event<Unit>> =
        _geofenceLimitForFreeVersionReached

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
        _newSearchValueReceived.value =
            Event(string)
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
        val geofence =
            GeofenceEntity(catalogId = currentCatalogId,
                latitude = point.latitude,
                longitude = point.longitude, radius = nextGeofenceRaidus)
        disposables.add(
            // получаем общее кол-во геозон и кидает искл. если больше допустимого лимита
            localRepository.getTotalGeofenceCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { geofenceCount ->
                    if (geofenceCount >= geofenceAppLimit) {
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
                    registerGeofences(context,
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
                    Toast.makeText(context,
                        context.getString(R.string.geofence_success,
                            geofenceAppLimit - (geofenceCount + 1),
                            geofenceAppLimit),
                        Toast.LENGTH_SHORT).show()
                }, {
                    // если была ошибка то либо выводим тост ошибкой и с советом проверить включен
                    // ли gps, либо (если ошибка неизвестна) просто уведомляем пользователя об ошибке
                    when {
                        it is GeofenceLimitException -> {
                            _geofenceLimitForFreeVersionReached.value =
                                Event(Unit)
                        }
                        it is ApiException && it.message == GEOFENCE_UNAVAILABLE_ERROR_MESSAGE -> {
                            showLongToast(context,
                                context.getString(R.string.geofence_api_error))
                        }
                        else -> {
                            showLongToast(context,
                                context.getString(R.string.adding_geofence_failed))
                        }
                    }
                }))
    }

    fun onCircleClick(geofenceId: Long) {
        localRepository.removeGeofenceById(geofenceId)
        unregisterGeofence(context, geofenceId) {
            Toast.makeText(context,
                context.getString(R.string.geofence_unreg_success),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun onClearAllClick() {
        localRepository.removeAllGeofencesFromCatalog(currentCatalogId)
        unregisterAllGeofences(context,
            currentCatalogId) { context ->
            Toast.makeText(context,
                context.getString(R.string.all_geofences_unreg_success),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun onFindMyLocationClick() {
        _findMyLocationClicked.value =
            Event(Unit)
    }

    fun onZoomInClick() {
        _zoomInClicked.value = Event(Unit)
    }

    fun onZoomOutClick() {
        _zoomOutClicked.value = Event(Unit)
    }
}

//class MapVmFactory(
//    private val localRepository: LocalRepository,
//    private val catalogId: Long,
//    private val catalogName: String,
//    private val groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
//    private val application: Application
//) :
//    ViewModelProvider.AndroidViewModelFactory(application) {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
//            return MapViewModel(
//                localRepository,
//                catalogId,
//                catalogName,
//                groupExpandStates,
//                application) as T
//        }
//        throw IllegalArgumentException("Wrong ViewModel class")
//    }
//}