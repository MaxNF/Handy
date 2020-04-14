package ru.netfantazii.handy.core.catalogs

import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.core.catalogs.usecases.*
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.SortOrder
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class CatalogsViewModel @Inject constructor(
    private val addNewCatalogToTheBeginningUseCase: AddNewCatalogToTheBeginningUseCase,
    private val addNewCatalogToTheEndUseCase: AddNewCatalogToTheEndUseCase,
    private val dragCatalogUseCase: DragCatalogUseCase,
    private val removeCatalogUseCase: RemoveCatalogUseCase,
    private val renameCatalogUseCase: RenameCatalogUseCase,
    private val realRemovePendingCatalogUseCase: RealRemovePendingCatalogUseCase,
    private val subscribeToCatalogsChangesUseCase: SubscribeToCatalogsChangesUseCase,
    private val undoRemovalUseCase: UndoRemovalUseCase,
    private val loadCatalogNetInfoUseCase: LoadCatalogNetInfoUseCase
) :
    ViewModel(),
    CatalogClickHandler, CatalogStorage, OverlayActions, DialogClickHandler {

    override lateinit var overlayBuffer: BufferObject

    private var notFilteredCatalogList = listOf<Catalog>()
    private var filteredCatalogList = listOf<Catalog>()
        set(value) {
            field = value
            _redrawRecyclerView.value = Event(Unit)
        }
    val shouldHintBeShown: Boolean
        get() = filteredCatalogList.isEmpty()

    private val disposables = CompositeDisposable()

    private val _redrawRecyclerView = MutableLiveData<Event<Unit>>()
    val redrawRecyclerView: LiveData<Event<Unit>> = _redrawRecyclerView

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

    private val newCatalogsObserver =
        Observer<Pair<List<Catalog>, List<Catalog>>> { (filtered, notFiltered) ->
            filteredCatalogList = filtered
            notFilteredCatalogList = notFiltered
        }

    init {
        subscribeToCatalogsChangesUseCase.filteredAndNotFilteredCatalogs.observeForever(
            newCatalogsObserver)
    }

    fun onCreateCatalogClick() {
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_CREATE, Catalog())
        _createCatalogClicked.value = Event(Unit)
    }

    override fun onCatalogClick(catalog: Catalog) {
        _catalogClicked.value = Event(catalog)
    }

    override fun onCatalogSwipeStart(catalog: Catalog) {
        _catalogSwipeStarted.value = Event(Unit)
    }

    override fun onCatalogSwipePerform(catalog: Catalog) {
        removeCatalogUseCase.removeCatalog(catalog, notFilteredCatalogList.toMutableList())
    }

    override fun onCatalogSwipeFinish(catalog: Catalog) {
        _catalogSwipeFinished.value = Event(catalog)
    }

    override fun onCatalogSwipeCancel(catalog: Catalog) {
        _catalogSwipeCanceled.value = Event(catalog)
    }

    override fun onCatalogEditClick(catalog: Catalog) {
        overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_RENAME, catalog)
        _catalogEditClicked.value = Event(catalog.getCopy())
    }

    override fun onCatalogDragSucceed(fromPosition: Int, toPosition: Int) {
        dragCatalogUseCase.dragCatalog(notFilteredCatalogList.toMutableList(),
            fromPosition,
            toPosition)
    }

    override fun onCatalogNotificationClick(catalog: Catalog) {
        _catalogNotificationClicked.value = Event(catalog)
    }

    override fun onCatalogShareClick(catalog: Catalog) {
        _catalogShareClicked.value = Event(catalog)
    }

    override fun onCatalogEnvelopeClick(catalog: Catalog) {
        disposables.add(loadCatalogNetInfoUseCase.fetchCatalogWithNetInfo(catalog).subscribe { netInfo ->
            _catalogAndNetInfoReceived.value = Event(Pair(catalog, netInfo))
            _catalogEnvelopeClicked.value = Event(Unit)
        })
    }

    override fun getCatalogList(): List<Catalog> = filteredCatalogList

    override fun onOverlayBackgroundClick() {
        _overlayBackgroundClicked.value = Event(Unit)
    }

    override fun onOverlayEnterClick() {
        val catalog = overlayBuffer.bufferObject as Catalog
        when (overlayBuffer.action) {
            OVERLAY_ACTION_CATALOG_CREATE -> {
                if (currentSortOrder == SortOrder.NEWEST_FIRST) {
                    addNewCatalogToTheBeginningUseCase.addNewCatalogToTheBeginning(catalog,
                        notFilteredCatalogList.toMutableList())
                } else {
                    addNewCatalogToTheEndUseCase.addNewCatalogToTheEnd(catalog,
                        notFilteredCatalogList.toMutableList())
                }
            }
            OVERLAY_ACTION_CATALOG_RENAME -> renameCatalogUseCase.renameCatalog(catalog)
            else -> throw UnsupportedOperationException("Unsupported action for catalog objects")
        }
        _overlayEnterClicked.value = Event(Unit)
    }

    fun undoRemoval() {
        undoRemovalUseCase.undoRemoval()?.let {
            disposables.add(it.subscribe())
        }
    }

    fun onFragmentStop() {
        realRemovePendingCatalogUseCase.realRemovePendingCatalog(notFilteredCatalogList.toMutableList())
    }

    override fun onCleared() {
        disposables.clear()
        subscribeToCatalogsChangesUseCase.filteredAndNotFilteredCatalogs.removeObserver(
            newCatalogsObserver)
    }
}