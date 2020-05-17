package ru.netfantazii.handy.ui.groupsandproducts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.data.model.Event
import ru.netfantazii.handy.data.usecases.catalog.UpdateCatalogExpandStatesUseCase
import ru.netfantazii.handy.data.usecases.group.*
import ru.netfantazii.handy.ui.preferences.currentSortOrder
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.data.model.SortOrder
import ru.netfantazii.handy.data.usecases.product.*
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.ui.base.*
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class GroupsAndProductsViewModel @Inject constructor(
    @CatalogId private val currentCatalogId: Long,
    var groupExpandStates: RecyclerViewExpandableItemManager.SavedState,
    private val addNewGroupToTheBeginningUseCase: AddNewGroupToTheBeginningUseCase,
    private val addNewGroupToTheEndUseCase: AddNewGroupToTheEndUseCase,
    private val addNewProductToTheBeginningUseCase: AddNewProductToTheBeginningUseCase,
    private val addNewProductToTheEndUseCase: AddNewProductToTheEndUseCase,
    private val changeProductStatusUseCase: ChangeProductStatusUseCase,
    private val dragGroupUseCase: DragGroupUseCase,
    private val dragProductUseCase: DragProductUseCase,
    private val removeGroupUseCase: RemoveGroupUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val renameGroupUseCase: RenameGroupUseCase,
    private val renameProductUseCase: RenameProductUseCase,
    private val subscribeToGroupsChangesUseCase: SubscribeToGroupsChangesUseCase,
    private val undoGroupRemovalUseCase: UndoGroupRemovalUseCase,
    private val undoProductRemovalUseCase: UndoProductRemovalUseCase,
    private val buyAllProductsUseCase: BuyAllProductsUseCase,
    private val markAllNotBoughtUseCase: MarkAllNotBoughtUseCase,
    private val removeAllGroupsUseCase: RemoveAllGroupsUseCase,
    private val updateCatalogExpandStatesUseCase: UpdateCatalogExpandStatesUseCase,
    private val calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase
) : ViewModel(), GroupClickHandler, ProductClickHandler, GroupStorage,
    OverlayActions,
    DialogClickHandler {
    override lateinit var overlayBuffer: BufferObject
    private var notFilteredGroupList = listOf<Group>()
    private var filteredGroupList = listOf<Group>()
        set(value) {
            field = value
            _redrawRecyclerView.value =
                Event(Unit)
        }
    val allFilteredProducts: List<Product>
        get() = filteredGroupList.flatMap { it.productList }
    val shouldHintBeShown: Boolean
        get() = filteredGroupList.isEmpty() && filteredGroupList.isEmpty()
    private val disposables = CompositeDisposable()

    private val _redrawRecyclerView = MutableLiveData<Event<Unit>>()
    val redrawRecyclerView: LiveData<Event<Unit>> = _redrawRecyclerView

    private val _productClicked = MutableLiveData<Event<Product>>()
    val productClicked: LiveData<Event<Product>> = _productClicked

    private val _productSwipeStarted = MutableLiveData<Event<Unit>>()
    val productSwipeStarted: LiveData<Event<Unit>> = _productSwipeStarted

    private val _productSwipePerformed = MutableLiveData<Event<Product>>()
    val productSwipePerformed: LiveData<Event<Product>> = _productSwipePerformed

    private val _productSwipeFinished = MutableLiveData<Event<Product>>()
    val productSwipeFinished: LiveData<Event<Product>> = _productSwipeFinished

    private val _productSwipeCanceled = MutableLiveData<Event<Product>>()
    val productSwipeCanceled: LiveData<Event<Product>> = _productSwipeCanceled

    private val _productEditClicked = MutableLiveData<Event<Product>>()
    val productEditClicked: LiveData<Event<Product>> = _productEditClicked

    private val _productDragSucceed = MutableLiveData<Event<Unit>>()
    val productDragSucceed: LiveData<Event<Unit>> = _productDragSucceed

    private val _createProductClicked = MutableLiveData<Event<Unit>>()
    val createProductClicked: LiveData<Event<Unit>> = _createProductClicked

    private val _groupClicked = MutableLiveData<Event<Int>>()
    val groupClicked: LiveData<Event<Int>> = _groupClicked

    private val _groupCreateProductClicked = MutableLiveData<Event<Int>>()
    val groupCreateProductClicked: LiveData<Event<Int>> = _groupCreateProductClicked

    private val _groupSwipeStarted = MutableLiveData<Event<Unit>>()
    val groupSwipeStarted: LiveData<Event<Unit>> = _groupSwipeStarted

    private val _groupSwipePerformed = MutableLiveData<Event<Group>>()
    val groupSwipePerformed: LiveData<Event<Group>> = _groupSwipePerformed

    private val _groupSwipeFinished = MutableLiveData<Event<Group>>()
    val groupSwipeFinished: LiveData<Event<Group>> = _groupSwipeFinished

    private val _groupSwipeCanceled = MutableLiveData<Event<Group>>()
    val groupSwipeCanceled: LiveData<Event<Group>> = _groupSwipeCanceled

    private val _groupEditClicked = MutableLiveData<Event<Int>>()
    val groupEditClicked: LiveData<Event<Int>> = _groupEditClicked

    private val _groupDragSucceed = MutableLiveData<Event<Unit>>()
    val groupDragSucceed: LiveData<Event<Unit>> = _groupDragSucceed

    private val _createGroupClicked = MutableLiveData<Event<Unit>>()
    val createGroupClicked: LiveData<Event<Unit>> = _createGroupClicked

    private val _overlayBackgroundClicked = MutableLiveData<Event<Unit>>()
    val overlayBackgroundClicked: LiveData<Event<Unit>> = _overlayBackgroundClicked

    private val _overlayEnterClicked = MutableLiveData<Event<Unit>>()
    val overlayEnterClicked: LiveData<Event<Unit>> = _overlayEnterClicked

    private val _deleteAllClicked = MutableLiveData<Event<Unit>>()
    val deleteAllClicked: LiveData<Event<Unit>> = _deleteAllClicked

    private val _cancelAllClicked = MutableLiveData<Event<Unit>>()
    val cancelAllClicked: LiveData<Event<Unit>> = _cancelAllClicked

    private val _buyAllClicked = MutableLiveData<Event<Unit>>()
    val buyAllClicked: LiveData<Event<Unit>> = _buyAllClicked

    private val newGroupsObserver =
        Observer<Pair<List<Group>, List<Group>>> { (filtered, notFiltered) ->
            filteredGroupList = filtered
            notFilteredGroupList = notFiltered
        }

    init {
        subscribeToGroupsChangesUseCase.filteredAndNotFilteredGroups.observeForever(
            newGroupsObserver)
    }

    override fun onProductClick(group: Group, product: Product) {
        changeProductStatusUseCase.changeProductStatus(group,
            product,
            notFilteredGroupList.toMutableList())
        _productClicked.value = Event(product)
    }

    override fun onProductSwipeStart(product: Product) {
        _productSwipeStarted.value = Event(Unit)
    }

    override fun onProductSwipePerform(group: Group, product: Product) {
        removeProductUseCase.removeProduct(group, product)
    }

    override fun onProductSwipeFinish(group: Group, product: Product) {
        // необходимо выполнить следующий код, чтобы корректно пересчитать состояние группы после
        // удаление продукта и не уронить программу (вероятно, можно сделать это в юзкейсе удаления
        // продукта, но тогда потребуется копирование группы со списком, что не особо то и лучше
        group.productList.removeAt(product.position)
        calculateAndChangeGroupPositionUseCase.calculateGroupPosition(group,
            notFilteredGroupList.toMutableList())
        _productSwipeFinished.value =
            Event(product)
    }

    override fun onProductSwipeCancel(product: Product) {
        _productSwipeCanceled.value =
            Event(product)
    }

    override fun onProductEditClick(product: Product) {
        // передаем в буфер копию, чтобы не подтвержденные результаты изменений не отражались на продукте
        overlayBuffer = BufferObject(
            OVERLAY_ACTION_PRODUCT_RENAME,
            product.getCopy())
        _productEditClicked.value =
            Event(product)
    }

    override fun onProductDragSucceed(
        fromGroup: Group,
        fromPosition: Int,
        toGroup: Group,
        toPosition: Int
    ) {
        dragProductUseCase.dragProduct(fromGroup,
            fromPosition,
            toGroup,
            toPosition,
            notFilteredGroupList.toMutableList())
        _productDragSucceed.value = Event(Unit)
    }

    fun onCreateGroupClick() {
        val newGroup =
            Group(catalogId = currentCatalogId)
        overlayBuffer = BufferObject(
            OVERLAY_ACTION_GROUP_CREATE,
            newGroup)
        _createGroupClicked.value = Event(Unit)
    }

    override fun onGroupClick(group: Group) {
        _groupClicked.value =
            Event(filteredGroupList.indexOf(group))
    }

    override fun onGroupSwipeStart(group: Group) {
        _groupSwipeStarted.value = Event(Unit)
    }

    override fun onGroupSwipePerform(group: Group) {
        removeGroupUseCase.removeGroup(group, notFilteredGroupList.toMutableList())
    }

    override fun onGroupSwipeFinish(group: Group) {
        _groupSwipeFinished.value = Event(group)
    }

    override fun onGroupSwipeCancel(group: Group) {
        _groupSwipeCanceled.value = Event(group)
    }

    override fun onGroupEditClick(group: Group) {
        // передаем в буфер копию, чтобы не подтвержденные результаты изменений не отражались на продукте
        overlayBuffer = BufferObject(
            OVERLAY_ACTION_GROUP_RENAME,
            group.getCopy())
        _groupEditClicked.value =
            Event(filteredGroupList.indexOf(group))
    }

    override fun onGroupDragSucceed(fromPosition: Int, toPosition: Int) {
        dragGroupUseCase.dragGroup(fromPosition, toPosition, notFilteredGroupList.toMutableList())
        _groupDragSucceed.value = Event(Unit)
    }

    fun onCreateProductClick() {
        // Помещаем в буфер продукт для группы несортированных товаров (самая верхняя).
        // Поэтому id группы берем всегда у groupList[0]
        overlayBuffer =
            BufferObject(OVERLAY_ACTION_PRODUCT_CREATE,
                Product(catalogId = currentCatalogId,
                    groupId = notFilteredGroupList[0].id))
        _createProductClicked.value = Event(Unit)
    }

    override fun onGroupCreateProductClick(group: Group) {
        // если группа null, то создаем в дефолтной группе
        overlayBuffer =
            BufferObject(OVERLAY_ACTION_PRODUCT_CREATE,
                Product(catalogId = currentCatalogId,
                    groupId = group.id))
        _groupCreateProductClicked.value =
            Event(getGroupList().indexOf(group))
    }

    override fun getGroupList(): List<Group> {
        return filteredGroupList
    }

    override fun onOverlayBackgroundClick() {
        _overlayBackgroundClicked.value =
            Event(Unit)
    }

    override fun onOverlayEnterClick() {
        val entity = overlayBuffer.bufferObject
        when (overlayBuffer.action) {
            OVERLAY_ACTION_GROUP_CREATE -> {
                if (entity !is Group) throw UnsupportedOperationException("Buffer object should be group.")
                if (currentSortOrder == SortOrder.NEWEST_FIRST) {
                    addNewGroupToTheBeginningUseCase.addNewGroup(entity,
                        notFilteredGroupList.toMutableList())
                } else {
                    addNewGroupToTheEndUseCase.addNewGroup(entity,
                        notFilteredGroupList.toMutableList())
                }
                _overlayEnterClicked.value =
                    Event(Unit)
            }
            OVERLAY_ACTION_GROUP_RENAME -> {
                if (entity !is Group) throw UnsupportedOperationException("Buffer object should be group.")
                renameGroupUseCase.renameGroup(entity)
                _overlayEnterClicked.value =
                    Event(Unit)
            }
            OVERLAY_ACTION_PRODUCT_CREATE -> {
                if (entity !is Product) throw UnsupportedOperationException("Buffer object should be product.")
                if (currentSortOrder == SortOrder.NEWEST_FIRST) {
                    addNewProductToTheBeginningUseCase.addNewProduct(entity,
                        notFilteredGroupList.toMutableList())
                } else {
                    addNewProductToTheEndUseCase.addNewProduct(entity,
                        notFilteredGroupList.toMutableList())
                }
                overlayBuffer.replaceObject(Product(catalogId = currentCatalogId,
                    groupId = entity.groupId))
            }
            OVERLAY_ACTION_PRODUCT_RENAME -> {
                if (entity !is Product) throw UnsupportedOperationException("Buffer object should be product.")
                renameProductUseCase.renameProduct(entity)
                _overlayEnterClicked.value =
                    Event(Unit)
            }
        }
    }

    fun undoProductRemoval() {
        undoProductRemovalUseCase.undoProductRemoval(notFilteredGroupList.toMutableList())
    }

    fun undoGroupRemoval() {
        undoGroupRemovalUseCase.undoGroupRemoval(notFilteredGroupList.toMutableList())
    }

    fun onCancelAllClick() {
        _cancelAllClicked.value = Event(Unit)
    }

    fun onBuyAllClick() {
        _buyAllClicked.value = Event(Unit)
    }

    fun onDeleteAllClick() {
        _deleteAllClicked.value = Event(Unit)
    }

    override fun onCancelAllYesClick() {
        markAllNotBoughtUseCase.markAllNotBought(notFilteredGroupList)
    }

    override fun onBuyAllYesClick() {
        buyAllProductsUseCase.buyAll(notFilteredGroupList)
    }

    override fun onDeleteAllYesClick() {
        removeAllGroupsUseCase.removeAll(notFilteredGroupList)
    }

    override fun onCleared() {
        disposables.clear()
        subscribeToGroupsChangesUseCase.filteredAndNotFilteredGroups.removeObserver(
            newGroupsObserver)
    }

    fun saveExpandStateToDb(groupExpandStates: RecyclerViewExpandableItemManager.SavedState) {
        updateCatalogExpandStatesUseCase.updateCatalogExpandStates(currentCatalogId,
            groupExpandStates)
    }

}