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
import ru.netfantazii.handy.db.BuyStatus
import ru.netfantazii.handy.db.Group
import ru.netfantazii.handy.db.Product
import ru.netfantazii.handy.extensions.*

class GroupsAndProductsViewModel(private val localRepository: LocalRepository) : ViewModel(),
    GroupClickHandler, ProductClickHandler, GroupStorage, OverlayActions {
    private val TAG = "GroupAndProductsViewMod"
    private var groupList = mutableListOf<Group>()
        set(value) {
            field = value
            val allProducts = value.flatMap { it.productList }
            val shouldHintBeShown = allProducts.isEmpty()
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
        disposables.add(localRepository.getGroups(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                groupList = it
            })
    }

    override fun getGroupList(): List<Group> = groupList

    override fun onProductClick(product: Product) {
        Log.d(TAG, "onProductClick: ")
        product.buyStatus =
            if (product.buyStatus == BuyStatus.NOT_BOUGHT) BuyStatus.BOUGHT else BuyStatus.NOT_BOUGHT
        localRepository.updateProduct(product)
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
                group.productList.subList(removedProductIndex + 1, group.productList.size)
            listForUpdating.shiftPositionsToLeft()
            localRepository.removeAndUpdateProducts(product, listForUpdating)
        }
        lastRemovedProduct = product
    }

    override fun onProductSwipeFinish(product: Product) {
        Log.d(TAG, "onProductSwipeFinish: ")
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
        val firstProductList = groupList[fromGroup].productList
        val secondProductList = groupList[toGroup].productList
        if (firstProductList == secondProductList) {
            localRepository.updateAllProducts(firstProductList.subListModified(fromPosition,
                toPosition))
        } else {
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
        }
        _productDragSucceed.value = Event(Unit)
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
            val listForUpdating = groupList.subList(removedGroupIndex + 1, groupList.size)
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
        localRepository.updateAllGroups(groupList.subListModified(fromPosition, toPosition))
        _groupDragSucceed.value = Event(Unit)
    }

    override fun onOverlayBackgroundClick() {
        Log.d(TAG, "onOverlayBackgroundClick: ")
        _overlayBackgroundClicked.value = Event(Unit)
    }

    override fun onOverlayEnterClick() {
        when (overlayBuffer.action) {
            OVERLAY_ACTION_GROUP_CREATE -> {
                groupList.shiftPositionsToRight(1)
                localRepository.addAndUpdateGroups(overlayBuffer.bufferObject as Group, groupList)
            }
            OVERLAY_ACTION_GROUP_RENAME -> {
                localRepository.addAndUpdateGroups(overlayBuffer.bufferObject as Group, groupList)
            }
            OVERLAY_ACTION_PRODUCT_CREATE -> {
                TODO()
            }
            OVERLAY_ACTION_PRODUCT_RENAME -> {
                TODO()
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
    }
}

class GroupsAndProductsVmFactory(private val localRepository: LocalRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsAndProductsViewModel::class.java)) {
            return GroupsAndProductsViewModel(localRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}