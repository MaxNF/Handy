package ru.netfantazii.handy.core.groupsandproducts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.*
import ru.netfantazii.handy.extensions.*
import java.lang.UnsupportedOperationException
import java.util.NoSuchElementException

class GroupsAndProductsViewModel(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long
) : ViewModel(),
    GroupClickHandler, ProductClickHandler, GroupStorage, OverlayActions, DialogClickHandler {
    private val TAG = "GroupsAndProductsViewMo"
    private var groupList = mutableListOf<Group>()
        set(groups) {
            field = groups
            onNewDataReceive(groups)
        }
    private var filteredGroupList = listOf<Group>()
    val shouldHintBeShown: Boolean
        get() {
            return getAllProducts().isEmpty() && filteredGroupList.isEmpty()
        }

    lateinit var groupExpandStates: RecyclerViewExpandableItemManager.SavedState
    fun isGroupExpandStatesInitialized() = ::groupExpandStates.isInitialized

    private val disposables = CompositeDisposable()
    private var lastRemovedGroup: Group? = null
    private var lastRemovedProduct: Product? = null

    override lateinit var overlayBuffer: BufferObject

    private val _newDataReceived = MutableLiveData<Event<Unit>>()
    val newDataReceived: LiveData<Event<Unit>> = _newDataReceived

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

    init {
        subscribeToGroupChanges()
    }

    private fun subscribeToGroupChanges() {
        disposables.add(localRepository.getGroups(currentCatalogId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                groupList = it
            })
    }

    private fun onNewDataReceive(groups: MutableList<Group>) {
        filteredGroupList = getFilteredGroupList(groupList)
        _newDataReceived.value = Event(Unit)
    }

    override fun getGroupList(): List<Group> = filteredGroupList

    fun getAllProducts(): List<Product> = filteredGroupList.flatMap { it.productList }

    private fun getFilteredGroupList(groupList: MutableList<Group>): List<Group> {
        return when {
            groupList.isEmpty() -> groupList
            groupList[0].productList.isEmpty() -> {
                groupList.slice(1..groupList.lastIndex)
            }
            else -> groupList
        }
    }

    override fun onProductClick(group: Group, product: Product) {
        Log.d(TAG, "onProductClick: ")
        updateProductStatusAndPositions(product, group.productList)
        checkGroupStatusAndUpdatePosition(group, groupList)
        _productClicked.value = Event(product)
    }

    override fun onProductSwipeStart(product: Product) {
        Log.d(TAG, "onProductSwipeStart: ")
        _productSwipeStarted.value = Event(Unit)
    }

    override fun onProductSwipePerform(group: Group, product: Product) {
        Log.d(TAG, "onProductSwipePerform: ")
        if (group.productList.size == 1) {
            localRepository.removeProduct(product)
        } else {
            val removedProductIndex = group.productList.indexOf(product)
            val listForUpdating =
                group.productList.slice((removedProductIndex + 1)..group.productList.lastIndex)
            listForUpdating.shiftPositionsToLeft()
            localRepository.removeAndUpdateProducts(product, listForUpdating)
        }
        lastRemovedProduct = product
    }

    override fun onProductSwipeFinish(group: Group, product: Product) {
        Log.d(TAG, "onProductSwipeFinish: ")
        group.productList.remove(product)
        checkGroupStatusAndUpdatePosition(group, groupList)
        _productSwipeFinished.value = Event(product)
    }

    override fun onProductSwipeCancel(product: Product) {
        Log.d(TAG, "onProductSwipeCancel: ")
        _productSwipeCanceled.value = Event(product)
    }

    override fun onProductEditClick(product: Product) {
        Log.d(TAG, "onProductEditClick: ")
        val productToEdit = product.getCopy()
        overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_RENAME, productToEdit)
        _productEditClicked.value = Event(product)
    }

    override fun onProductDragSucceed(
        fromGroup: Group,
        fromPosition: Int,
        toGroup: Group,
        toPosition: Int
    ) {
        Log.d(TAG, "onProductDragSucceed: ")
        val firstProductList = fromGroup.productList
        val secondProductList = toGroup.productList
        if (firstProductList == secondProductList) {
            firstProductList.moveAndReassignPositions(fromPosition, toPosition)
            localRepository.updateAllProducts(firstProductList.sliceModified(fromPosition,
                toPosition))
        } else {
            firstProductList[fromPosition].groupId = toGroup.id
            moveBetweenListsAndReassignPositions(firstProductList,
                fromPosition,
                secondProductList,
                toPosition)
            val listForUpdating = mutableListOf<Product>()
            with(listForUpdating) {
                addAll(firstProductList)
                addAll(secondProductList)
            }
            localRepository.updateAllProducts(listForUpdating)
            checkGroupStatusAndUpdatePosition(fromGroup, groupList)
            checkGroupStatusAndUpdatePosition(toGroup, groupList)
        }
        _productDragSucceed.value = Event(Unit)
    }

    private fun checkGroupStatusAndUpdatePosition(group: Group, groupList: MutableList<Group>) {
        if (group.groupType != GroupType.ALWAYS_ON_TOP && group.isStatusChanged()) {
            val previousStatus = group.buyStatus
            val fromPosition = group.position
            val toPosition =
                if (previousStatus == BuyStatus.NOT_BOUGHT) groupList.lastIndex else 1 // позиция самой верхней стандартной группы (0 относится к неизменяемой группе несорт. товаров)
            groupList.moveAndReassignPositions(fromPosition, toPosition)
            val groupListForUpdating = groupList.sliceModified(fromPosition, toPosition)
            localRepository.updateAllGroups(groupListForUpdating)
        }
    }

    private fun updateProductStatusAndPositions(
        product: Product,
        productList: MutableList<Product>
    ) {
        val previousStatus = product.buyStatus
        val fromPosition = product.position
        val toPosition: Int
        if (previousStatus == BuyStatus.NOT_BOUGHT) {
            toPosition = productList.lastIndex
            product.buyStatus = BuyStatus.BOUGHT
        } else {
            toPosition = 0
            product.buyStatus = BuyStatus.NOT_BOUGHT
        }
        productList.moveAndReassignPositions(fromPosition, toPosition)
        val productListForUpdating = productList.sliceModified(fromPosition,
            toPosition).toMutableList()
        localRepository.updateAllProducts(productListForUpdating)
    }

    fun onCreateProductClick() {
        Log.d(TAG, "onCreateProductClick: ")
        // Помещаем в буфер продукт для группы несортированных товаров (самая верхняя).
        // Поэтому id группы берем всегда у groupList[0]
        overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_CREATE,
            createNewProduct(currentCatalogId,
                groupList[0].id,
                getNewProductPosition(groupList[0].productList)))
        _createProductClicked.value = Event(Unit)
    }

    private fun getNewProductPosition(productList: List<Product>): Int {
        return when (currentSortOrder) {
            SortOrder.NEWEST_FIRST -> 0
            SortOrder.OLDEST_FIRST -> getFirstNotBoughtProductPosition(productList)
        }
    }

    private fun getFirstNotBoughtProductPosition(productList: List<Product>): Int {
        val lastNotBoughtProduct = productList.findLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
        return if (lastNotBoughtProduct != null) lastNotBoughtProduct.position + 1 else 0
    }

    private fun getNewGroupPosition(groupList: List<Group>): Int {
        return when (currentSortOrder) {
            SortOrder.NEWEST_FIRST -> 1 // добавляем после дефолтной группы!
            SortOrder.OLDEST_FIRST -> getFirstNotBoughtGroupPosition(groupList)
        }
    }

    private fun getFirstNotBoughtGroupPosition(groupList: List<Group>): Int {
        val listToFindGroup = groupList.subList(1, groupList.size)
        val lastNotBoughtGroup = listToFindGroup.findLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
        return if (lastNotBoughtGroup != null) lastNotBoughtGroup.position + 1 else 1
    }

    override fun onGroupClick(group: Group) {
        Log.d(TAG, "onGroupClick: ")
        _groupClicked.value = Event(filteredGroupList.indexOf(group))
    }

    override fun onGroupSwipeStart(group: Group) {
        Log.d(TAG, "onGroupSwipeStart: ")
        _groupSwipeStarted.value = Event(Unit)
    }

    override fun onGroupSwipePerform(group: Group) {
        Log.d(TAG, "onGroupSwipePerform: ")
        if (groupList.size == 1) {
            localRepository.removeGroup(group)
        } else {
            val removedGroupIndex = groupList.indexOf(group)
            val listForUpdating = groupList.slice(removedGroupIndex + 1..groupList.lastIndex)
            listForUpdating.shiftPositionsToLeft()
            localRepository.removeAndUpdateGroups(group, listForUpdating)
        }
        lastRemovedGroup = group
    }

    override fun onGroupSwipeFinish(group: Group) {
        Log.d(TAG, "onGroupSwipeFinish: ")
        _groupSwipeFinished.value = Event(group)
    }

    override fun onGroupSwipeCancel(group: Group) {
        Log.d(TAG, "onGroupSwipeCancel: ")
        _groupSwipeCanceled.value = Event(group)
    }

    override fun onGroupEditClick(group: Group) {
        Log.d(TAG, "onGroupEditClick: ")
        val groupToEdit = group.getCopy()
        overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_RENAME, groupToEdit)
        val groupPosition = getGroupList().indexOf(group)
        _groupEditClicked.value = Event(groupPosition)
    }

    override fun onGroupDragSucceed(fromPosition: Int, toPosition: Int) {
        Log.d(TAG, "onGroupDragSucceed: ")
        groupList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllGroups(groupList.sliceModified(fromPosition, toPosition))
        _groupDragSucceed.value = Event(Unit)
    }

    override fun onOverlayBackgroundClick() {
        Log.d(TAG, "onOverlayBackgroundClick: ")
        _overlayBackgroundClicked.value = Event(Unit)
    }

    override fun onOverlayEnterClick() {
        Log.d(TAG, "onOverlayEnterClick: ")
        when (overlayBuffer.action) {
            OVERLAY_ACTION_GROUP_CREATE -> {
                addNewGroupToRepo()
            }
            OVERLAY_ACTION_GROUP_RENAME -> {
                renameGroupAndUpdate()
            }
            OVERLAY_ACTION_PRODUCT_CREATE -> {
                addNewProductToRepo()
            }
            OVERLAY_ACTION_PRODUCT_RENAME -> {
                renameProductAndUpdate()
            }
        }
    }

    private fun addNewGroupToRepo() {
        val groupToAdd = overlayBuffer.bufferObject as Group
        groupList.add(groupToAdd.position, groupToAdd)
        groupList.reassignPositions()
        localRepository.addAndUpdateGroups(groupToAdd, groupList)
        _overlayEnterClicked.value = Event(Unit)
    }

    private fun renameGroupAndUpdate() {
//        localRepository.addAndUpdateGroups(overlayBuffer.bufferObject as Group, groupList)
        localRepository.updateGroup(overlayBuffer.bufferObject as Group)
        _overlayEnterClicked.value = Event(Unit)
    }

    private fun addNewProductToRepo() {
        val product = overlayBuffer.bufferObject as Product
        val productList = groupList.find { it.id == product.groupId }?.productList
            ?: throw UnsupportedOperationException("Group is not found")
        productList.add(product.position, product)
        productList.reassignPositions()
        localRepository.addAndUpdateProducts(overlayBuffer.bufferObject as Product,
            productList)
        overlayBuffer.replaceObject(createNewProduct(currentCatalogId,
            product.groupId,
            getNewProductPosition(productList)))
    }

    private fun renameProductAndUpdate() {
        localRepository.updateProduct(overlayBuffer.bufferObject as Product)
        _overlayEnterClicked.value = Event(Unit)
    }

    override fun onGroupCreateProductClick(group: Group) {
        Log.d(TAG, "onGroupCreateProductClick: ")
        overlayBuffer =
            BufferObject(OVERLAY_ACTION_PRODUCT_CREATE,
                createNewProduct(currentCatalogId,
                    group.id,
                    getNewProductPosition(group.productList)))
        _groupCreateProductClicked.value = Event(getGroupList().indexOf(group))
    }

    fun onCreateGroupClick() {
        Log.d(TAG, "onCreateGroupClick: ")
        val newGroup =
            Group(catalogId = currentCatalogId, position = getNewGroupPosition(groupList))
        overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_CREATE, newGroup)
        _createGroupClicked.value = Event(Unit)
    }

    private fun createNewProduct(catalogId: Long, groupId: Long, position: Int) =
        Product(catalogId = catalogId, groupId = groupId, position = position)

    fun undoProductRemoval() {
        Log.d(TAG, "undoProductRemoval: ")
        lastRemovedProduct?.let {
            val restoredProductPosition = it.position
            val restoredProductGroupId = it.groupId
            val listWithThisProduct =
                groupList.find { group -> group.id == restoredProductGroupId }?.productList
                    ?: throw NoSuchElementException("Group with id#${restoredProductGroupId} is not found")
            if (listWithThisProduct.isNotEmpty() && restoredProductPosition < listWithThisProduct.size) {
                val listForUpdating =
                    listWithThisProduct.slice(restoredProductPosition..listWithThisProduct.lastIndex)
                listForUpdating.shiftPositionsToRight()
                localRepository.addAndUpdateProducts(it, listForUpdating)
            } else {
                localRepository.addProduct(it)
            }
        }
    }

    fun undoGroupRemoval() {
        Log.d(TAG, "undoGroupRemoval: ")
        lastRemovedGroup?.let { group ->
            if (groupList.isNotEmpty() && group.position < groupList.size) {
                val listForUpdating = groupList.slice(group.position..groupList.lastIndex)
                listForUpdating.shiftPositionsToRight()
                localRepository.addGroupWithProductsAndUpdateAll(group, listForUpdating)
            } else {
                localRepository.addGroupWithProducts(group)
            }
        }
    }

    fun onCancelAllClick() {
        Log.d(TAG, "onCancelAllClick: ")
        _cancelAllClicked.value = Event(Unit)
    }

    fun onBuyAllClick() {
        Log.d(TAG, "onBuyAllClick: ")
        _buyAllClicked.value = Event(Unit)
    }

    fun onDeleteAllClick() {
        Log.d(TAG, "onDeleteAllClick: ")
        _deleteAllClicked.value = Event(Unit)
    }

    override fun onCancelAllYesClick() {
        Log.d(TAG, "cancelAll: ")
        val allProducts = groupList.flatMap { it.productList }
        allProducts.forEach { it.buyStatus = BuyStatus.NOT_BOUGHT }
        localRepository.updateAllProducts(allProducts)
    }

    override fun onBuyAllYesClick() {
        Log.d(TAG, "buyAll: ")
        val allProducts = groupList.flatMap { it.productList }
        allProducts.forEach { it.buyStatus = BuyStatus.BOUGHT }
        localRepository.updateAllProducts(allProducts)
    }

    override fun onDeleteAllYesClick() {
        Log.d(TAG, "deleteAll: ")
        localRepository.removeAllGroups(groupList)
    }

    fun saveExpandStateToDb(groupExpandStates: RecyclerViewExpandableItemManager.SavedState) {
        localRepository.updateGroupExpandStates(currentCatalogId, groupExpandStates)
        Log.d(TAG, "saveExpandStateToDb: ")
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun onFragmentStop(groupExpandStates: RecyclerViewExpandableItemManager.SavedState) {
        saveExpandStateToDb(groupExpandStates)
    }
}

class GroupsAndProductsVmFactory(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsAndProductsViewModel::class.java)) {
            return GroupsAndProductsViewModel(localRepository, currentCatalogId) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}