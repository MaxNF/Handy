package ru.netfantazii.handy.core.groupsandproducts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.db.*
import ru.netfantazii.handy.extensions.*
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
import java.util.NoSuchElementException

class GroupsAndProductsViewModel(
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long
) : ViewModel(),
    GroupClickHandler, ProductClickHandler, GroupStorage, OverlayActions {
    private val TAG = "GroupsAndProductsViewMo"
    private var groupList = mutableListOf<Group>()
        set(groups) {
            field = groups
            val allProducts = groups.flatMap { it.productList }
            val shouldHintBeShown = allProducts.isEmpty() && groups.size == 1
            _newDataReceived.value = Event(shouldHintBeShown)
        }

    private val disposables = CompositeDisposable()
    private var lastRemovedGroup: Group? = null
    private var lastRemovedProduct: Product? = null

    override lateinit var overlayBuffer: BufferObject

    private val _newDataReceived = MutableLiveData<Event<Boolean>>()
    val newDataReceived: LiveData<Event<Boolean>> = _newDataReceived

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

    private val _groupSwipeStarted = MutableLiveData<Event<Unit>>()
    val groupSwipeStarted: LiveData<Event<Unit>> = _groupSwipeStarted

    private val _groupSwipePerformed = MutableLiveData<Event<Group>>()
    val groupSwipePerformed: LiveData<Event<Group>> = _groupSwipePerformed

    private val _groupSwipeFinished = MutableLiveData<Event<Group>>()
    val groupSwipeFinished: LiveData<Event<Group>> = _groupSwipeFinished

    private val _groupSwipeCanceled = MutableLiveData<Event<Group>>()
    val groupSwipeCanceled: LiveData<Event<Group>> = _groupSwipeCanceled

    private val _groupEditClicked = MutableLiveData<Event<Group>>()
    val groupEditClicked: LiveData<Event<Group>> = _groupEditClicked

    private val _groupDragSucceed = MutableLiveData<Event<Unit>>()
    val groupDragSucceed: LiveData<Event<Unit>> = _groupDragSucceed

    private val _createGroupClicked = MutableLiveData<Event<Unit>>()
    val createGroupClicked: LiveData<Event<Unit>> = _createGroupClicked

    private val _overlayBackgroundClicked = MutableLiveData<Event<Unit>>()
    val overlayBackgroundClicked: LiveData<Event<Unit>> = _overlayBackgroundClicked

    private val _overlayEnterClicked = MutableLiveData<Event<Unit>>()
    val overlayEnterClicked: LiveData<Event<Unit>> = _overlayEnterClicked

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

    override fun getGroupList(): List<Group> = groupList

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
        overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_RENAME, product)
        _productEditClicked.value = Event(product)
    }

    override fun onProductDragSucceed(
        fromGroup: Int,
        fromPosition: Int,
        toGroup: Int,
        toPosition: Int
    ) {
        Log.d(TAG, "onProductDragSucceed: ")
        val firstGroup = groupList[fromGroup]
        val secondGroup = groupList[toGroup]
        val firstProductList = firstGroup.productList
        val secondProductList = secondGroup.productList
        if (firstProductList == secondProductList) {
            firstProductList.moveAndReassignPositions(fromPosition, toPosition)
            localRepository.updateAllProducts(firstProductList.sliceModified(fromPosition,
                toPosition))
        } else {
            firstProductList[fromPosition].groupId = secondGroup.id
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
            checkGroupStatusAndUpdatePosition(firstGroup, groupList)
            checkGroupStatusAndUpdatePosition(secondGroup, groupList)
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
        if (previousStatus == BuyStatus.NOT_BOUGHT){
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
            createNewProduct(currentCatalogId, groupList[0].id))
        _createProductClicked.value = Event(Unit)
    }

    override fun onGroupClick(groupPosition: Int) {
        Log.d(TAG, "onGroupClick: ")
        _groupClicked.value = Event(groupPosition)
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
        overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_RENAME, group)
        _groupEditClicked.value = Event(group)
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
                groupList.shiftPositionsToRight(1)
                localRepository.addAndUpdateGroups(overlayBuffer.bufferObject as Group, groupList)
                _overlayEnterClicked.value = Event(Unit)
            }
            OVERLAY_ACTION_GROUP_RENAME -> {
                localRepository.addAndUpdateGroups(overlayBuffer.bufferObject as Group, groupList)
                _overlayEnterClicked.value = Event(Unit)
            }
            OVERLAY_ACTION_PRODUCT_CREATE -> {
                val groupId = (overlayBuffer.bufferObject as Product).groupId
                val listForUpdating = groupList.find { it.id == groupId }?.productList
                    ?: throw UnsupportedOperationException("List for updating can not be null")
                listForUpdating.shiftPositionsToRight()
                localRepository.addAndUpdateProducts(overlayBuffer.bufferObject as Product,
                    listForUpdating)
                overlayBuffer.replaceObject(createNewProduct(currentCatalogId, groupId))
            }
            OVERLAY_ACTION_PRODUCT_RENAME -> {
                localRepository.updateProduct(overlayBuffer.bufferObject as Product)
                _overlayEnterClicked.value = Event(Unit)
            }
        }
    }

    override fun onGroupCreateProductClick(groupId: Long) {
        Log.d(TAG, "onGroupCreateProductClick: ")
        overlayBuffer =
            BufferObject(OVERLAY_ACTION_PRODUCT_CREATE, createNewProduct(currentCatalogId, groupId))
        _createProductClicked.value = Event(Unit)
    }

    fun onCreateGroupClick() {
        Log.d(TAG, "onCreateGroupClick: ")
        val newGroup = Group(catalogId = currentCatalogId)
        overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_CREATE, newGroup)
        _createGroupClicked.value = Event(Unit)
    }

    private fun createNewProduct(catalogId: Long, groupId: Long) =
        Product(catalogId = catalogId, groupId = groupId)

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
        lastRemovedGroup?.let {
            val restoredGroupPosition = it.position
            if (groupList.isNotEmpty() && restoredGroupPosition < groupList.size) {
                val listForUpdating = groupList.slice(restoredGroupPosition..groupList.lastIndex)
                listForUpdating.shiftPositionsToRight()
                localRepository.addGroupWithProductsAndUpdateAll(it, listForUpdating)
            } else {
                localRepository.addGroupWithProducts(it)
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
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