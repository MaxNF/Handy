package ru.netfantazii.handy.core.catalogs.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject

class RemoveCatalogUseCaseTest : CatalogUseCasesTestBase() {

    private lateinit var removeCatalogUseCase: RemoveCatalogUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        removeCatalogUseCase = RemoveCatalogUseCase(localRepository, pendingRemovedObject)
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
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]
        removeCatalogUseCase.removeCatalog(addedCatalogs[0], addedCatalogs)
        val resultList = localRepository.getCatalogs().test().values()[0]
        assertThat(resultList.size, `is`(3))
        assertThat(resultList[0].name, `is`("1"))
        assertThat(resultList[1].name, `is`("2"))
        assertThat(resultList[2].name, `is`("3"))
        assertThat((pendingRemovedObject.entity as Catalog).name, `is`("1"))
    }

    @Test
    fun removeTwoCatalogs_secondCatalogAddedToPendingObjectAndFirstReallyRemoved() {
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]
        removeCatalogUseCase.removeCatalog(addedCatalogs[0], addedCatalogs)
        removeCatalogUseCase.removeCatalog(addedCatalogs[1], addedCatalogs)
        val resultList = localRepository.getCatalogs().test().values()[0]
        assertThat(resultList.size, `is`(2))
        assertThat(resultList[0].name, `is`("2"))
        assertThat(resultList[1].name, `is`("3"))
        assertThat((pendingRemovedObject.entity as Catalog).name, `is`("2"))
    }
}