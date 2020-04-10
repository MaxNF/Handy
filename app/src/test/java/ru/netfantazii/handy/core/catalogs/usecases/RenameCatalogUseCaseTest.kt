package ru.netfantazii.handy.core.catalogs.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.createFakeCatalog

class RenameCatalogUseCaseTest : CatalogUseCasesTestBase() {

    private lateinit var renameCatalogUseCase: RenameCatalogUseCase

    @Before
    fun createUseCase() {
        renameCatalogUseCase = RenameCatalogUseCase(localRepository)
    }

    @Test
    fun renameCatalog() {
        val catalog1 = createFakeCatalog("1", 0)
        val catalog2 = createFakeCatalog("2", 1)
        val catalog3 = createFakeCatalog("3", 2)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        localRepository.addCatalog(catalog3)
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]

        val renamedCatalog1 = addedCatalogs[0].apply { name = "renamed1" }
        val renamedCatalog2 = addedCatalogs[1].apply { name = "renamed2" }
        val renamedCatalog3 = addedCatalogs[2].apply { name = "renamed3" }
        renameCatalogUseCase.renameCatalog(renamedCatalog1)
        renameCatalogUseCase.renameCatalog(renamedCatalog2)
        renameCatalogUseCase.renameCatalog(renamedCatalog3)

        val resultList = localRepository.getCatalogs().test().values()[0]

        assertThat(resultList[0].id, `is`(1L))
        assertThat(resultList[0].name, `is`("renamed1"))

        assertThat(resultList[1].id, `is`(2L))
        assertThat(resultList[1].name, `is`("renamed2"))

        assertThat(resultList[2].id, `is`(3L))
        assertThat(resultList[2].name, `is`("renamed3"))
    }
}