package ru.netfantazii.handy.data.usecases.catalog

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.getOrAwaitValue

class AddNewToTheBeginningUseCaseTest : UseCasesTestBase() {

    private lateinit var addNewCatalogToTheBeginningUseCase: AddNewCatalogToTheBeginningUseCase

    @Before
    fun createUseCase() {
        addNewCatalogToTheBeginningUseCase = AddNewCatalogToTheBeginningUseCase(localRepository)
    }

    @Test
    fun addNewCatalogToTheBeginning_catalogAddedPositionsUpdated() {
        val catalog1 = createFakeCatalog("1")
        localRepository.addCatalog(catalog1)
        val addedCatalogs = localRepository.getCatalogs().getOrAwaitValue()

        val catalog2 = createFakeCatalog("2")
        addNewCatalogToTheBeginningUseCase.addNewCatalogToTheBeginning(catalog2, addedCatalogs)
        val resultList = localRepository.getCatalogs().getOrAwaitValue()

        assertThat(resultList.size, `is`(2))

        assertThat(resultList[0].name, `is`("2"))
        assertThat(resultList[0].position, `is`(0))

        assertThat(resultList[1].name, `is`("1"))
        assertThat(resultList[1].position, `is`(1))
    }
}