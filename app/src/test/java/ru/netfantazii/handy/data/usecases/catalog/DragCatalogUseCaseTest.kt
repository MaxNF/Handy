package ru.netfantazii.handy.data.usecases.catalog

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.getOrAwaitValue

class DragCatalogUseCaseTest : UseCasesTestBase() {

    private lateinit var dragCatalogUseCase: DragCatalogUseCase

    @Before
    fun createUseCase() {
        dragCatalogUseCase = DragCatalogUseCase(localRepository)
    }

    @Test
    fun dragCatalogTest_catalogsPositionsUpdated() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        val catalog3 = createFakeCatalog("3", position = 2)
        val catalog4 = createFakeCatalog("4", position = 3)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        localRepository.addCatalog(catalog3)
        localRepository.addCatalog(catalog4)
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()

        dragCatalogUseCase.dragCatalog(addedCatalogs,
            addedCatalogs[0].position,
            addedCatalogs[2].position)

        val resultList = localRepository.getCatalogs().getOrAwaitValue()

        assertThat(resultList[0].position, `is`(0))
        assertThat(resultList[0].name, `is`("2"))

        assertThat(resultList[1].position, `is`(1))
        assertThat(resultList[1].name, `is`("3"))

        assertThat(resultList[2].position, `is`(2))
        assertThat(resultList[2].name, `is`("1"))

        assertThat(resultList[3].position, `is`(3))
        assertThat(resultList[3].name, `is`("4"))
    }
}