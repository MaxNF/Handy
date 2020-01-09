package ru.netfantazii.handy.core.catalogs

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.db.Catalog
import ru.netfantazii.handy.extensions.*


class CatalogsViewModel(private val localRepository: LocalRepository) : ViewModel(),
    CatalogClickHandler, CatalogStorage, OverlayActions {
    private val TAG = "CatalogsViewModel"
    private var catalogList = mutableListOf<Catalog>()
        set(value) {
            field = value
            onNewDataReceive(value)
        }

    private val disposables = CompositeDisposable()

    private var lastRemovedObject: Catalog? = null

    override lateinit var overlayBuffer: BufferObject

    private val _newDataReceived = MutableLiveData<Event<Boolean>>()
    val newDataReceived: LiveData<Event<Boolean>> = _newDataReceived

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

    init {
        subscribeToCatalogsChanges()
    }

    private fun onNewDataReceive(newList: MutableList<Catalog>) {
        Log.d(TAG, "onNewDataReceive: ")
        lastRemovedObject?.let { newList.remove(it) }
        val shouldHintBeShown = newList.isEmpty()
        _newDataReceived.value = Event(shouldHintBeShown)
    }

    private fun subscribeToCatalogsChanges() {
        disposables.add(localRepository.getCatalogs()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                catalogList = it
            })
    }

    override fun getCatalogList(): List<Catalog> = catalogList


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
        tempRemove(catalog)
        if (lastRemovedObject == null) {
            onNewDataReceive(catalogList)
        } else {
            realRemove(lastRemovedObject!!)
        }
        lastRemovedObject = catalog
    }

    private fun tempRemove(catalog: Catalog) {
        if (catalogList.size > 1 && catalog.position != catalogList.lastIndex) {
            val listForUpdating =
                catalogList.slice(catalog.position + 1..catalogList.lastIndex)
            listForUpdating.shiftPositionsToLeft()
        }
        catalogList.remove(catalog)
    }

    private fun realRemove(catalog: Catalog) {
        // удаляем объект из буфера и обновляем остальные каталоги в бд,
        // т.к. их позиция могли измениться с последнего обновления
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
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_RENAME, catalog)
        _catalogEditClicked.value = Event(catalog)
    }

    override fun onCatalogDragSucceed(fromPosition: Int, toPosition: Int) {
        Log.d(TAG, "onCatalogDragSucceed: ")
        catalogList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllCatalogs(catalogList.sliceModified(fromPosition, toPosition))
        _catalogDragSucceeded.value = Event(Unit)
    }

    fun onCreateCatalogClick() {
        Log.d(TAG, "createCatalog: ")
        val newCatalog = Catalog()
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_CREATE, newCatalog)
        _createCatalogClicked.value = Event(Unit)
    }

    fun undoRemoval() {
        Log.d(TAG, "undoRemoval: ")
        lastRemovedObject?.let {
            val removedCatalogPosition = it.position
            catalogList.shiftPositionsToRight(it.position)
            catalogList.add(removedCatalogPosition, it)
            lastRemovedObject = null
            onNewDataReceive(catalogList)
        }
    }

    override fun onOverlayBackgroundClick() {
        Log.d(TAG, "onOverlayBackgroundClick: ")
        _overlayBackgroundClicked.value = Event(Unit)
    }

    override fun onOverlayEnterClick() {
        if (overlayBuffer.action == OVERLAY_ACTION_CATALOG_CREATE) {
            catalogList.shiftPositionsToRight()
            localRepository.addAndUpdateCatalogs(overlayBuffer.bufferObject as Catalog, catalogList)
        } else {
            localRepository.updateCatalog(overlayBuffer.bufferObject as Catalog)
        }
        _overlayEnterClicked.value = Event(Unit)
    }

    override fun onCleared() {
        disposables.clear()
        lastRemovedObject?.let { realRemove(it) }
    }
}

class CatalogsVmFactory(private val localRepository: LocalRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogsViewModel::class.java)) {
            return CatalogsViewModel(localRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}