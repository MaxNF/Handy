package ru.netfantazii.handy.core.catalogs.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.getOrAwaitValue

class RealRemovePendingCatalogUseCaseTest : CatalogUseCasesTestBase() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var realRemovePendingCatalogUseCase: RealRemovePendingCatalogUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        realRemovePendingCatalogUseCase =
            RealRemovePendingCatalogUseCase(localRepository, pendingRemovedObject)
    }

    @Test
    fun realRemovePendingCatalog_catalogRemovedFromDatabase() {
        val catalog1 = createFakeCatalog("1")
        pendingRemovedObject.insertEntity(catalog1, false)
        localRepository.addCatalog(catalog1)
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()
        assertThat(addedCatalogs.size, `is`(1))

        val removeResult = realRemovePendingCatalogUseCase.realRemovePendingCatalog(addedCatalogs)
        val resultList = localRepository.getCatalogs().getOrAwaitValue()

        assertThat(removeResult, `is`(true))
        assertThat(pendingRemovedObject.entity, `is`(nullValue()))
        assertThat(resultList.size, `is`(0))
    }

    @Test
    fun realRemovePendingCatalog_pendingObjectIsNull_nothingRemoved() {
        val catalog1 = createFakeCatalog("1")
        localRepository.addCatalog(catalog1)
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()
        assertThat(addedCatalogs.size, `is`(1))

        val removeResult = realRemovePendingCatalogUseCase.realRemovePendingCatalog(addedCatalogs)
        val resultList = localRepository.getCatalogs().getOrAwaitValue()

        assertThat(removeResult, `is`(false))
        assertThat(resultList.size, `is`(1))
    }
}