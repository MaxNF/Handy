package ru.netfantazii.handy.core.groupsandproducts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import org.hamcrest.CoreMatchers.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.ArgumentMatchers
import ru.netfantazii.handy.core.catalogs.usecases.UpdateCatalogExpandStatesUseCase
import ru.netfantazii.handy.core.groupsandproducts.usecases.*
import org.mockito.Mockito.*
import ru.netfantazii.handy.createFakeGroup
import ru.netfantazii.handy.createFakeProduct
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.getOrAwaitValue
import ru.netfantazii.handy.any
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.data.SortOrder

class GroupsAndProductsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var addNewGroupToTheBeginningUseCase: AddNewGroupToTheBeginningUseCase
    private lateinit var addNewGroupToTheEndUseCase: AddNewGroupToTheEndUseCase
    private lateinit var addNewProductToTheBeginningUseCase: AddNewProductToTheBeginningUseCase
    private lateinit var addNewProductToTheEndUseCase: AddNewProductToTheEndUseCase
    private lateinit var changeProductStatusUseCase: ChangeProductStatusUseCase
    private lateinit var dragGroupUseCase: DragGroupUseCase
    private lateinit var dragProductUseCase: DragProductUseCase
    private lateinit var removeGroupUseCase: RemoveGroupUseCase
    private lateinit var removeProductUseCase: RemoveProductUseCase
    private lateinit var renameGroupUseCase: RenameGroupUseCase
    private lateinit var renameProductUseCase: RenameProductUseCase
    private lateinit var subscribeToGroupsChangesUseCase: SubscribeToGroupsChangesUseCase
    private lateinit var undoGroupRemovalUseCase: UndoGroupRemovalUseCase
    private lateinit var undoProductRemovalUseCase: UndoProductRemovalUseCase
    private lateinit var buyAllProductsUseCase: BuyAllProductsUseCase
    private lateinit var markAllNotBoughtUseCase: MarkAllNotBoughtUseCase
    private lateinit var removeAllGroupsUseCase: RemoveAllGroupsUseCase
    private lateinit var updateCatalogExpandStatesUseCase: UpdateCatalogExpandStatesUseCase

    private lateinit var viewModel: GroupsAndProductsViewModel

    @Before
    fun createMocksAndViewModel() {
        addNewGroupToTheBeginningUseCase = mock(AddNewGroupToTheBeginningUseCase::class.java)
        addNewGroupToTheEndUseCase = mock(AddNewGroupToTheEndUseCase::class.java)
        addNewProductToTheBeginningUseCase = mock(AddNewProductToTheBeginningUseCase::class.java)
        addNewProductToTheEndUseCase = mock(AddNewProductToTheEndUseCase::class.java)
        changeProductStatusUseCase = mock(ChangeProductStatusUseCase::class.java)
        dragGroupUseCase = mock(DragGroupUseCase::class.java)
        dragProductUseCase = mock(DragProductUseCase::class.java)
        removeGroupUseCase = mock(RemoveGroupUseCase::class.java)
        removeProductUseCase = mock(RemoveProductUseCase::class.java)
        renameGroupUseCase = mock(RenameGroupUseCase::class.java)
        renameProductUseCase = mock(RenameProductUseCase::class.java)
        subscribeToGroupsChangesUseCase = mock(SubscribeToGroupsChangesUseCase::class.java)
        `when`(subscribeToGroupsChangesUseCase.filteredAndNotFilteredGroups)
            .thenReturn(MutableLiveData<Pair<List<Group>, List<Group>>>())

        undoGroupRemovalUseCase = mock(UndoGroupRemovalUseCase::class.java)
        undoProductRemovalUseCase = mock(UndoProductRemovalUseCase::class.java)
        buyAllProductsUseCase = mock(BuyAllProductsUseCase::class.java)
        markAllNotBoughtUseCase = mock(MarkAllNotBoughtUseCase::class.java)
        removeAllGroupsUseCase = mock(RemoveAllGroupsUseCase::class.java)
        updateCatalogExpandStatesUseCase = mock(UpdateCatalogExpandStatesUseCase::class.java)

        val expandStates = mock(RecyclerViewExpandableItemManager.SavedState::class.java)
        viewModel = GroupsAndProductsViewModel(1,
            expandStates,
            addNewGroupToTheBeginningUseCase,
            addNewGroupToTheEndUseCase,
            addNewProductToTheBeginningUseCase,
            addNewProductToTheEndUseCase,
            changeProductStatusUseCase,
            dragGroupUseCase,
            dragProductUseCase,
            removeGroupUseCase,
            removeProductUseCase,
            renameGroupUseCase,
            renameProductUseCase,
            subscribeToGroupsChangesUseCase,
            undoGroupRemovalUseCase,
            undoProductRemovalUseCase,
            buyAllProductsUseCase,
            markAllNotBoughtUseCase,
            removeAllGroupsUseCase,
            updateCatalogExpandStatesUseCase)
    }

    @Test
    fun onProductClickTest_changeProductStatusUseCaseInvokedLiveDataUpdated() {
        viewModel.onProductClick(createFakeGroup(), createFakeProduct())
        val value = viewModel.productClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(changeProductStatusUseCase, times(1))
            .changeProductStatus(any(), any(), any())
    }

    @Test
    fun onProductSwipeStartTest_LiveDataUpdated() {
        viewModel.onProductSwipeStart(createFakeProduct())
        val value = viewModel.productSwipeStarted.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onProductSwipePerformTest_removeProductUseCaseInvoked() {
        viewModel.onProductSwipePerform(createFakeGroup(), createFakeProduct())
        verify(removeProductUseCase, times(1)).removeProduct(any(), any())
    }

    @Test
    fun onProductSwipeFinishTest_liveDataUpdated() {
        viewModel.onProductSwipeFinish(createFakeGroup(), createFakeProduct())
        val value = viewModel.productSwipeFinished.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onProductSwipeCancelTest_liveDataUpdated() {
        viewModel.onProductSwipeCancel(createFakeProduct())
        val value = viewModel.productSwipeCanceled.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onProductEditClickTest_bufferObjectUpdatedLiveDataUpdated() {
        val product = createFakeProduct()
        viewModel.onProductEditClick(product)
        val value = viewModel.productEditClicked.getOrAwaitValue()

        assertThat(viewModel.overlayBuffer.bufferObject as Product, `is`(product))
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onProductDragSucceedTest_dragProductUseCaseInvokedLiveDataUpdated() {
        viewModel.onProductDragSucceed(createFakeGroup(), 0, createFakeGroup(), 0)
        val value = viewModel.productDragSucceed.getOrAwaitValue()

        verify(dragProductUseCase, times(1)).dragProduct(any(),
            ArgumentMatchers.anyInt(),
            any(),
            ArgumentMatchers.anyInt(),
            ArgumentMatchers.anyList())
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupClickTest_liveDataUpdated() {
        viewModel.onGroupClick(createFakeGroup())
        val value = viewModel.groupClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupSwipeStartTest_liveDataUpdated() {
        viewModel.onGroupSwipeStart(createFakeGroup())
        val value = viewModel.groupSwipeStarted.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupSwipePerformTest_removeGroupUseCaseInvoked() {
        viewModel.onGroupSwipePerform(createFakeGroup())
        verify(removeGroupUseCase, times(1)).removeGroup(any(), ArgumentMatchers.anyList())
    }

    @Test
    fun onGroupSwipeFinishTest_liveDataUpdated() {
        viewModel.onGroupSwipeFinish(createFakeGroup())
        val value = viewModel.groupSwipeFinished.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupSwipeCancelTest_liveDataUpdated() {
        viewModel.onGroupSwipeCancel(createFakeGroup())
        val value = viewModel.groupSwipeCanceled.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupEditClickTest_bufferObjectUpdatedLiveDataUpdated() {
        val group = createFakeGroup()
        viewModel.onGroupEditClick(group)
        val value = viewModel.groupEditClicked.getOrAwaitValue()
        assertThat(viewModel.overlayBuffer.bufferObject as Group, `is`(group))
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onGroupDragSucceedTest_dragGroupUseCaseInvokedLiveDataUpdated() {
        viewModel.onGroupDragSucceed(0, 0)
        val value = viewModel.groupDragSucceed.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(dragGroupUseCase, times(1)).dragGroup(ArgumentMatchers.anyInt(),
            ArgumentMatchers.anyInt(), ArgumentMatchers.anyList())
    }

    @Test
    fun onGroupCreateProductClickTest_bufferObjectUpdatedLiveDataUpdated() {
        viewModel.onGroupCreateProductClick(createFakeGroup())
        val value = viewModel.groupCreateProductClicked.getOrAwaitValue()
        assertThat(viewModel.overlayBuffer.bufferObject, instanceOf(Product::class.java))
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onOverlayBackgroundClickTest_liveDataUpdated() {
        viewModel.onOverlayBackgroundClick()
        val value = viewModel.overlayBackgroundClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onOverlayEnterClickTest_withGroupCreateActionAndNewestSortOrder_addNewGroupToTheBeginningUseCaseInvokedLiveDataUpdated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_CREATE, createFakeGroup())
        currentSortOrder = SortOrder.NEWEST_FIRST
        viewModel.onOverlayEnterClick()

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(addNewGroupToTheBeginningUseCase, times(1)).addNewGroup(any(),
            ArgumentMatchers.anyList())
    }

    @Test
    fun onOverlayEnterClickTest_withGroupCreateActionAndOldestSortOrder_addNewGroupToTheEndUseCaseInvokedLiveDataUpdated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_CREATE, createFakeGroup())
        currentSortOrder = SortOrder.OLDEST_FIRST
        viewModel.onOverlayEnterClick()

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(addNewGroupToTheEndUseCase, times(1)).addNewGroup(any(),
            ArgumentMatchers.anyList())
    }

    @Test
    fun onOverlayEnterClickTest_withGroupRenameAction_renameGroupUseCaseInvokedLiveDataUpdated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_GROUP_RENAME, createFakeGroup())
        viewModel.onOverlayEnterClick()

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(renameGroupUseCase, times(1)).renameGroup(any())
    }

    @Test
    fun onOverlayEnterClickTest_withProductCreateActionAndNewestSortOrder_addNewProductToTheBeginningUseCaseInvoked() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_CREATE, createFakeProduct())
        currentSortOrder = SortOrder.NEWEST_FIRST
        viewModel.onOverlayEnterClick()

        verify(addNewProductToTheBeginningUseCase, times(1)).addNewProduct(any(),
            ArgumentMatchers.anyList())
    }

    @Test
    fun onOverlayEnterClickTest_withProductCreateActionAndOldestSortOrder_addNewProductToTheEndUseCaseInvoked() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_CREATE, createFakeProduct())
        currentSortOrder = SortOrder.OLDEST_FIRST
        viewModel.onOverlayEnterClick()

        verify(addNewProductToTheEndUseCase, times(1)).addNewProduct(any(),
            ArgumentMatchers.anyList())
    }

    @Test
    fun onOverlayEnterClickTest_withProductRenameAction_renameProductUseCaseInvokedLiveDataUpdated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_PRODUCT_RENAME, createFakeProduct())
        viewModel.onOverlayEnterClick()

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        verify(renameProductUseCase, times(1)).renameProduct(any())
    }


    @Test
    fun undoProductRemovalTest_undoProductRemovalUseCaseInvoked() {
        viewModel.undoProductRemoval()
        verify(undoProductRemovalUseCase, times(1)).undoProductRemoval(ArgumentMatchers.anyList())
    }

    @Test
    fun undoGroupRemovalTest_undoGroupRemovalUseCaseInvoked() {
        viewModel.undoGroupRemoval()
        verify(undoGroupRemovalUseCase, times(1)).undoGroupRemoval(ArgumentMatchers.anyList())
    }

    @Test
    fun onCancelAllClickTest_liveDataUpdated() {
        viewModel.onCancelAllClick()
        val value = viewModel.cancelAllClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onBuyAllClickTest_liveDataUpdated() {
        viewModel.onBuyAllClick()
        val value = viewModel.buyAllClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onDeleteAllClickTest_liveDataUpdated() {
        viewModel.onDeleteAllClick()
        val value = viewModel.deleteAllClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
    }

    @Test
    fun onCancelAllYesClickTest_markAllNotBoughtUseCaseInvoked() {
        viewModel.onCancelAllYesClick()
        verify(markAllNotBoughtUseCase, times(1)).markAllNotBought(ArgumentMatchers.anyList())
    }

    @Test
    fun onBuyAllYesClickTest_buyAllProductsUseCaseInvoked() {
        viewModel.onBuyAllYesClick()
        verify(buyAllProductsUseCase, times(1)).buyAll(ArgumentMatchers.anyList())
    }

    @Test
    fun onDeleteAllYesClickTest_removeAllGroupsUseCaseInvoked() {
        viewModel.onDeleteAllYesClick()
        verify(removeAllGroupsUseCase, times(1)).removeAll(ArgumentMatchers.anyList())
    }

    @Test
    fun saveExpandStateToDbTest_updateCatalogExpandStatesUseCaseInvoked() {
        val expandStates = mock(RecyclerViewExpandableItemManager.SavedState::class.java)
        viewModel.saveExpandStateToDb(expandStates)
        verify(updateCatalogExpandStatesUseCase, times(1)).updateCatalogExpandStates(
            ArgumentMatchers.anyLong(), any())
    }

    @Test
    fun onCreateGroupClickTest_bufferObjectUpdatedLiveDataUpdated() {
        viewModel.onCreateGroupClick()
        val value = viewModel.createGroupClicked.getOrAwaitValue()
        assertThat(value, `is`(notNullValue()))
        assertThat(viewModel.overlayBuffer.bufferObject, instanceOf(Group::class.java))
    }
}