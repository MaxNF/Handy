package ru.netfantazii.handy.core.catalogs

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.times
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues
import org.mockito.invocation.InvocationOnMock
import ru.netfantazii.handy.any
import ru.netfantazii.handy.core.BufferObject
import ru.netfantazii.handy.core.OVERLAY_ACTION_CATALOG_CREATE
import ru.netfantazii.handy.core.OVERLAY_ACTION_CATALOG_RENAME
import ru.netfantazii.handy.core.catalogs.usecases.*
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.SortOrder
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import ru.netfantazii.handy.getOrAwaitValue
import java.lang.Exception
import java.util.*

class CatalogsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var addNewCatalogToTheBeginningUseCase: AddNewCatalogToTheBeginningUseCase
    private lateinit var addNewCatalogToTheEndUseCase: AddNewCatalogToTheEndUseCase
    private lateinit var dragCatalogUseCase: DragCatalogUseCase
    private lateinit var removeCatalogUseCase: RemoveCatalogUseCase
    private lateinit var renameCatalogUseCase: RenameCatalogUseCase
    private lateinit var realRemovePendingCatalogUseCase: RealRemovePendingCatalogUseCase
    private lateinit var subscribeToCatalogsChangesUseCase: SubscribeToCatalogsChangesUseCase
    private lateinit var undoRemovalUseCase: UndoRemovalUseCase
    private lateinit var loadCatalogNetInfoUseCase: LoadCatalogNetInfoUseCase

    private lateinit var viewModel: CatalogsViewModel

    @Before
    fun createMocksAndViewModel() {
        addNewCatalogToTheBeginningUseCase =
            Mockito.mock(AddNewCatalogToTheBeginningUseCase::class.java)
        addNewCatalogToTheEndUseCase = Mockito.mock(AddNewCatalogToTheEndUseCase::class.java)
        dragCatalogUseCase = Mockito.mock(DragCatalogUseCase::class.java)
        removeCatalogUseCase = Mockito.mock(RemoveCatalogUseCase::class.java)
        renameCatalogUseCase = Mockito.mock(RenameCatalogUseCase::class.java)
        realRemovePendingCatalogUseCase = Mockito.mock(RealRemovePendingCatalogUseCase::class.java)
        subscribeToCatalogsChangesUseCase =
            Mockito.mock(SubscribeToCatalogsChangesUseCase::class.java)
        Mockito.`when`(subscribeToCatalogsChangesUseCase.filteredAndNotFilteredCatalogs)
            .thenReturn(MutableLiveData<Pair<List<Catalog>, List<Catalog>>>())

        undoRemovalUseCase = Mockito.mock(UndoRemovalUseCase::class.java)
        loadCatalogNetInfoUseCase = Mockito.mock(LoadCatalogNetInfoUseCase::class.java)
        Mockito.`when`(loadCatalogNetInfoUseCase.fetchCatalogWithNetInfo(any()))
            .thenReturn(Single.never<CatalogNetInfoEntity>())

        viewModel = CatalogsViewModel(
            addNewCatalogToTheBeginningUseCase,
            addNewCatalogToTheEndUseCase,
            dragCatalogUseCase,
            removeCatalogUseCase,
            renameCatalogUseCase,
            realRemovePendingCatalogUseCase,
            subscribeToCatalogsChangesUseCase,
            undoRemovalUseCase,
            loadCatalogNetInfoUseCase
        )
    }

    @Test
    fun onCreateCatalogClick_bufferObjectCreatedLiveDataValueCreated() {
        viewModel.onCreateCatalogClick()

        val createdCatalog = viewModel.overlayBuffer.bufferObject as Catalog
        val action = viewModel.overlayBuffer.action

        val emptyCatalog = Catalog()

        assertThat(createdCatalog, `is`(emptyCatalog))
        assertThat(action, `is`(OVERLAY_ACTION_CATALOG_CREATE))

        val value = viewModel.createCatalogClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogClick_liveDataValueCreated() {
        viewModel.onCatalogClick(createFakeCatalog())
        val value = viewModel.catalogClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogSwipeStart_liveDataValueCreated() {
        viewModel.onCatalogSwipeStart(createFakeCatalog())
        val value = viewModel.catalogSwipeStarted.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogSwipePerform_removeCatalogUseCaseInvoked() {
        viewModel.onCatalogSwipePerform(createFakeCatalog())
        Mockito.verify(removeCatalogUseCase, times(1)).removeCatalog(any(), any())
    }

    @Test
    fun onCatalogSwipeFinish_liveDataValueCreated() {
        viewModel.onCatalogSwipeFinish(createFakeCatalog())
        val value = viewModel.catalogSwipeFinished.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogSwipeCancel_liveDataValueCreated() {
        viewModel.onCatalogSwipeCancel(createFakeCatalog())
        val value = viewModel.catalogSwipeCanceled.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogEditClick_bufferObjectCreatedLiveDataValueCreated() {
        val emptyCatalog = Catalog()
        viewModel.onCatalogEditClick(emptyCatalog)

        val bufferCatalog = viewModel.overlayBuffer.bufferObject as Catalog
        val action = viewModel.overlayBuffer.action

        assertThat(bufferCatalog, `is`(emptyCatalog))
        assertThat(action, `is`(OVERLAY_ACTION_CATALOG_RENAME))

        val value = viewModel.catalogEditClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogDragSucceed_dragCatalogUseCaseInvoked() {
        viewModel.onCatalogDragSucceed(1, 2)
        Mockito.verify(dragCatalogUseCase, times(1))
            .dragCatalog(any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())
    }

    @Test
    fun onCatalogNotificationClick_liveDataValueCreated() {
        viewModel.onCatalogNotificationClick(createFakeCatalog())
        val value = viewModel.catalogNotificationClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogShareClick_liveDataValueCreated() {
        viewModel.onCatalogShareClick(createFakeCatalog())
        val value = viewModel.catalogShareClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onCatalogEnvelopeClick_loadCatalogNetInfoUseCaseInvoked() {
        viewModel.onCatalogEnvelopeClick(createFakeCatalog())
        Mockito.verify(loadCatalogNetInfoUseCase, times(1)).fetchCatalogWithNetInfo(any())
    }

    @Test
    fun getCatalogList_catalogListReturned() {
        val catalogList = viewModel.getCatalogList()
        assertThat(catalogList, `is`(not(nullValue())))
        assertThat(catalogList, instanceOf(List::class.java))
    }

    @Test
    fun onOverlayBackgroundClick_liveDataValueCreated() {
        viewModel.onOverlayBackgroundClick()
        val value = viewModel.overlayBackgroundClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onOverlayEnterClick_withCatalogCreateActionAndNewestSortOrder_addNewCatalogToTheBeginningUseCasesInvokedLiveDataValueCreated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_CREATE, createFakeCatalog())
        currentSortOrder = SortOrder.NEWEST_FIRST
        viewModel.onOverlayEnterClick()

        Mockito.verify(addNewCatalogToTheBeginningUseCase, times(1))
            .addNewCatalogToTheBeginning(any(), any())

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onOverlayEnterClick_withCatalogCreateActionAndOldestSortOrder_addNewCatalogToTheEndUseCasesInvokedLiveDataValueCreated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_CREATE, createFakeCatalog())
        currentSortOrder = SortOrder.OLDEST_FIRST
        viewModel.onOverlayEnterClick()

        Mockito.verify(addNewCatalogToTheEndUseCase, times(1))
            .addNewCatalogToTheEnd(any(), any())

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun onOverlayEnterClick_withCatalogRenameAction_renameCatalogUseCasesInvokedLiveDataValueCreated() {
        viewModel.overlayBuffer = BufferObject(OVERLAY_ACTION_CATALOG_RENAME, createFakeCatalog())
        viewModel.onOverlayEnterClick()

        Mockito.verify(renameCatalogUseCase, times(1))
            .renameCatalog(any())

        val value = viewModel.overlayEnterClicked.getOrAwaitValue()
        assertThat(value, `is`(not(nullValue())))
    }

    @Test
    fun undoRemoval_undoRemovalUseCaseInvoked() {
        viewModel.undoRemoval()
        Mockito.verify(undoRemovalUseCase, times(1)).undoRemoval()
    }

    @Test
    fun onFragmentStop_realRemovalUseCaseInvoked() {
        viewModel.onFragmentStop()
        Mockito.verify(realRemovePendingCatalogUseCase, times(1)).realRemovePendingCatalog(any())
    }
}