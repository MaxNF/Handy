package ru.netfantazii.handy.core.catalogs.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.createFakeCatalog

class RemoveCatalogUseCaseTest : CatalogUseCasesTestBase() {

    private lateinit var removeCatalogUseCase: RemoveCatalogUseCase

    @Before
    fun createUseCase() {
        removeCatalogUseCase = RemoveCatalogUseCase(localRepository)
    }

    @Test
    fun removeCatalogFromBeginning_catalogRemovedPositionsUpdated() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]

        removeCatalogUseCase.removeCatalog(addedCatalogs[0], addedCatalogs)
        val resultList = localRepository.getCatalogs().test().values()[0]
        assertThat(resultList.size, `is`(1))
        assertThat(resultList[0].id, `is`(2L))
        assertThat(resultList[0].name, `is`("2"))
        assertThat(resultList[0].position, `is`(0))
    }

    @Test
    fun removeCatalogFromEnd_catalogRemoved() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]

        removeCatalogUseCase.removeCatalog(addedCatalogs[1], addedCatalogs)
        val resultList = localRepository.getCatalogs().test().values()[0]
        assertThat(resultList.size, `is`(1))
        assertThat(resultList[0].id, `is`(1L))
        assertThat(resultList[0].name, `is`("1"))
        assertThat(resultList[0].position, `is`(0))
    }
}