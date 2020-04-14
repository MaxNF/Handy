package ru.netfantazii.handy.core.catalogs.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito
import ru.netfantazii.handy.core.notifications.alarm.usecases.UnregisterAlarmUseCase
import ru.netfantazii.handy.core.notifications.map.usecases.UnregisterAllGeofencesUseCase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.getOrAwaitValue

class RemoveCatalogUseCaseTest : CatalogUseCasesTestBase() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var unregisterAlarmUseCase: UnregisterAlarmUseCase
    private lateinit var unregisterAllGeofencesUseCase: UnregisterAllGeofencesUseCase
    private lateinit var cancelAssociatedNotificationUseCase: CancelAssociatedNotificationUseCase
    private lateinit var removeCatalogUseCase: RemoveCatalogUseCase
    private lateinit var realRemovePendingCatalogUseCase: RealRemovePendingCatalogUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        unregisterAlarmUseCase = Mockito.mock(UnregisterAlarmUseCase::class.java)
        unregisterAllGeofencesUseCase = Mockito.mock(UnregisterAllGeofencesUseCase::class.java)
        cancelAssociatedNotificationUseCase =
            Mockito.mock(CancelAssociatedNotificationUseCase::class.java)
        realRemovePendingCatalogUseCase = RealRemovePendingCatalogUseCase(localRepository, pendingRemovedObject)
        removeCatalogUseCase = RemoveCatalogUseCase(
            pendingRemovedObject,
            unregisterAllGeofencesUseCase,
            unregisterAlarmUseCase,
            cancelAssociatedNotificationUseCase,
            realRemovePendingCatalogUseCase)

    }

    @Before
    fun populateRepository() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        val catalog3 = createFakeCatalog("3", position = 2)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        localRepository.addCatalog(catalog3)
    }

    @Test
    fun removeCatalog_catalogAddedToPendingObjectAndNotReallyRemoved() {
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()
        removeCatalogUseCase.removeCatalog(addedCatalogs[0], addedCatalogs)
        val resultList = localRepository.getCatalogs().getOrAwaitValue()
        assertThat(resultList.size, `is`(3))
        assertThat(resultList[0].name, `is`("1"))
        assertThat(resultList[1].name, `is`("2"))
        assertThat(resultList[2].name, `is`("3"))
        assertThat((pendingRemovedObject.entity as Catalog).name, `is`("1"))
    }

    @Test
    fun removeTwoCatalogs_secondCatalogAddedToPendingObjectAndFirstReallyRemoved() {
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()
        removeCatalogUseCase.removeCatalog(addedCatalogs[0], addedCatalogs)
        removeCatalogUseCase.removeCatalog(addedCatalogs[1], addedCatalogs)
        val resultList = localRepository.getCatalogs().getOrAwaitValue()
        assertThat(resultList.size, `is`(2))
        assertThat(resultList[0].name, `is`("2"))
        assertThat(resultList[1].name, `is`("3"))
        assertThat((pendingRemovedObject.entity as Catalog).name, `is`("2"))
    }
}