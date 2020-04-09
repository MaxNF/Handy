package ru.netfantazii.handy.core.catalogs

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.extensions.*
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import ru.netfantazii.handy.data.database.GeofenceEntity
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.repositories.LocalRepository
import java.util.*
import javax.inject.Inject

class CatalogsViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    @ApplicationContext private val context: Context
) :
    ViewModel(),
    CatalogClickHandler, CatalogStorage, OverlayActions, DialogClickHandler {
    private val TAG = "CatalogsViewModel"
    private var catalogList = mutableListOf<Catalog>()
        set(value) {
            Log.d(TAG, ": ")
            field = value
            onNewDataReceive(value)
        }
    private var filteredCatalogList = listOf<Catalog>()
    val shouldHintBeShown: Boolean
        get() = filteredCatalogList.isEmpty()

    private val disposables = CompositeDisposable()

    private var lastRemovedObject: Catalog? = null

    override lateinit var overlayBuffer: BufferObject

    private val _newDataReceived = MutableLiveData<Event<Unit>>()
    val newDataReceived: LiveData<Event<Unit>> = _newDataReceived

    private val _catalogClicked = MutableLiveData<Event<Catalog>>()
    val catalogClicked: LiveData<Event<Catalog>> = _catalogClicked

    private val _catalogSwipeStarted = MutableLiveData<Event<Unit>>()
    val catalogSwipeStarted: LiveData<Event<Unit>> = _catalogSwipeStarted

    private val _catalogSwipePerformed = MutableLiveData<Event<Catalog>>()
    val catalogSwipePerformed: LiveData<Event<Catalog>> = _catalogSwipePerformed

    private val _catalogSwipeFinished = MutableLiveData<Event<Catalog>>()
    val catalogSwipeFinished: LiveData<Event<Catalog>> = _catalogSwipeFinished

    private val _catalogSwipeCanceled = MutableLiveData<Event<Catalog>>()
    val catalogSwipeCanceled: LiveData<Event<Catalog>> = _catalogSwipeCanceled

    private val _catalogEditClicked = MutableLiveData<Event<Catalog>>()
    val catalogEditClicked: LiveData<Event<Catalog>> = _catalogEditClicked

    private val _catalogDragSucceeded = MutableLiveData<Event<Unit>>()
    val catalogDragSucceeded: LiveData<Event<Unit>> = _catalogDragSucceeded

    private val _createCatalogClicked = MutableLiveData<Event<Unit>>()
    val createCatalogClicked: LiveData<Event<Unit>> = _createCatalogClicked

    private val _overlayBackgroundClicked = MutableLiveData<Event<Unit>>()
    val overlayBackgroundClicked: LiveData<Event<Unit>> = _overlayBackgroundClicked

    private val _overlayEnterClicked = MutableLiveData<Event<Unit>>()
    val overlayEnterClicked: LiveData<Event<Unit>> = _overlayEnterClicked

    private val _catalogNotificationClicked = MutableLiveData<Event<Catalog>>()
    val catalogNotificationClicked: LiveData<Event<Catalog>> = _catalogNotificationClicked

    private val _catalogShareClicked = MutableLiveData<Event<Catalog>>()
    val catalogShareClicked: LiveData<Event<Catalog>> = _catalogShareClicked

    private val _catalogEnvelopeClicked = MutableLiveData<Event<Unit>>()
    val catalogEnvelopeClicked: LiveData<Event<Unit>> = _catalogEnvelopeClicked

    private val _catalogAndNetInfoReceived =
        MutableLiveData<Event<Pair<Catalog, CatalogNetInfoEntity>>>()
    override val catalogAndNetInfoReceived: LiveData<Event<Pair<Catalog, CatalogNetInfoEntity>>> =
        _catalogAndNetInfoReceived


    init {
        subscribeToCatalogsChanges()
    }

    private fun onNewDataReceive(newList: MutableList<Catalog>) {
        Log.d(TAG, "onNewDataReceive: ")
        filteredCatalogList = getFilteredCatalogList(newList)
        _newDataReceived.value = Event(Unit)
    }

    private fun subscribeToCatalogsChanges() {
        disposables.add(localRepository.getCatalogs()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                catalogList = it
            })
    }

    private fun getFilteredCatalogList(catalogList: MutableList<Catalog>): List<Catalog> {
        val listToFilter = mutableListOf<Catalog>()
        listToFilter.addAll(catalogList)
        lastRemovedObject?.let { listToFilter.remove(it) }
        return listToFilter
    }

    override fun getCatalogList(): List<Catalog> = filteredCatalogList


    override fun onCatalogClick(catalog: Catalog) {
        Log.d(TAG, "onCatalogClick: ")
        _catalogClicked.value = Event(catalog)
    }

    override fun onCatalogSwipeStart(catalog: Catalog) {
        Log.d(TAG, "onCatalogSwipeStart: ")
        _catalogSwipeStarted.value = Event(Unit)
    }

    override fun onCatalogSwipePerform(catalog: Catalog) {
        Log.d(TAG, "onCatalogSwipePerform: ")
        if (lastRemovedObject == null) {
            lastRemovedObject = catalog
            onNewDataReceive(catalogList)
        } else {
            val previousObject = lastRemovedObject
            lastRemovedObject = catalog
            realRemove(previousObject!!)
        }
        cancelAssociatedNotifications(catalog.id)
        unregisterAllGeofences(context, catalog.id, null)
        unregisterAlarm(context, catalog.id)
    }

    private fun realRemove(catalog: Catalog) {
        catalogList.remove(catalog)
        catalogList.reassignPositions()
        localRepository.removeAndUpdateCatalogs(catalog, catalogList)
    }

    override fun onCatalogSwipeFinish(catalog: Catalog) {
        Log.d(TAG, "onCatalogSwipeFinish: ")
        _catalogSwipeFinished.value = Event(catalog)
    }

    override fun onCatalogSwipeCancel(catalog: Catalog) {
        Log.d(TAG, "onCatalogSwipeCancel: ")
        _catalogSwipeCanceled.value = Event(catalog)
    }

    override fun onCatalogEditClick(catalog: Catalog) {
        Log.d(TAG, "onCatalogEditClick: ")
        val catalogToEdit = catalog.getCopy()
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_RENAME, catalogToEdit)
        _catalogEditClicked.value = Event(catalog)
    }

    override fun onCatalogDragSucceed(fromPosition: Int, toPosition: Int) {
        Log.d(TAG, "onCatalogDragSucceed: from $fromPosition, to $toPosition ")
        catalogList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllCatalogs(catalogList.sliceModified(fromPosition, toPosition))
        _catalogDragSucceeded.value = Event(Unit)
    }

    fun onCreateCatalogClick() {
        Log.d(TAG, "createCatalog: ")
        val newCatalog = Catalog(position = getNewCatalogPosition(catalogList))
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_CREATE, newCatalog)
        _createCatalogClicked.value = Event(Unit)
    }

    fun undoRemoval() {
        Log.d(TAG, "undoRemoval: ")

        lastRemovedObject?.let { catalog ->
            localRepository.getGeofences(catalog.id)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable { geofenceEntities ->
                    registerGeofences(context,
                        geofenceEntities,
                        catalog.id,
                        catalog.name,
                        catalog.groupExpandStates)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                }
                .doFinally {
                    catalog.alarmTime?.let { alarmTime ->
                        restoreCatalogAlarm(catalog, alarmTime)
                    }
                    lastRemovedObject = null
                    onNewDataReceive(catalogList)
                }
                .subscribe()
        }
    }

    private fun cancelAssociatedNotifications(catalogId: Long) {
        val notificationId = catalogId.toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun restoreCatalogAlarm(catalog: Catalog, alarmTime: Calendar) {
        registerAlarm(context, catalog.id,
            catalog.name,
            catalog.groupExpandStates,
            alarmTime)
    }

    private fun restoreCatalogGeofences(catalog: Catalog, geofenceEntities: List<GeofenceEntity>) {
        registerGeofences(context,
            geofenceEntities,
            catalog.id,
            catalog.name,
            catalog.groupExpandStates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun onOverlayBackgroundClick() {
        Log.d(TAG, "onOverlayBackgroundClick: ")
        _overlayBackgroundClicked.value = Event(Unit)
    }

    override fun onOverlayEnterClick() {
        if (overlayBuffer.action == OVERLAY_ACTION_CATALOG_CREATE) {
            val catalogToAdd = overlayBuffer.bufferObject as Catalog
            catalogList.add(catalogToAdd.position, catalogToAdd)
            catalogList.reassignPositions()
            catalogList.remove(catalogToAdd)
            localRepository.addAndUpdateCatalogs(catalogToAdd, catalogList)
        } else {
            localRepository.updateCatalog(overlayBuffer.bufferObject as Catalog)
        }
        _overlayEnterClicked.value = Event(Unit)
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: ")
        disposables.clear()
    }

    override fun onCatalogNotificationClick(catalog: Catalog) {
        _catalogNotificationClicked.value = Event(catalog)
    }

    override fun onCatalogShareClick(catalog: Catalog) {
        _catalogShareClicked.value = Event(catalog)
    }

    fun onFragmentStop() {
        lastRemovedObject?.let { realRemove(it) }
    }

    override fun onCatalogEnvelopeClick(catalog: Catalog) {
        fetchCatalogWithNetInfo(catalog)
    }

    private fun fetchCatalogWithNetInfo(catalog: Catalog) {
        disposables.add(localRepository.getCatalogNetInfo(catalog.id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe { netInfo ->
                _catalogAndNetInfoReceived.value = Event(Pair(catalog, netInfo))
                _catalogEnvelopeClicked.value = Event(Unit)
            })
    }
}

//class CatalogsVmFactory(
//    private val localRepository: LocalRepository,
//    private val application: Application
//) :
//    ViewModelProvider.AndroidViewModelFactory(application) {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(CatalogsViewModel::class.java)) {
//            return CatalogsViewModel(localRepository, application) as T
//        }
//        throw IllegalArgumentException("Wrong ViewModel class")
//    }
//}