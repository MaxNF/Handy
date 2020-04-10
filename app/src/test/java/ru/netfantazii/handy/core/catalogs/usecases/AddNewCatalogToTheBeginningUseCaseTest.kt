package ru.netfantazii.handy.core.catalogs.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.repositories.LocalRepository

class AddNewCatalogToTheBeginningUseCaseTest : CatalogUseCasesTestBase() {

    private lateinit var addNewCatalogToTheBeginningUseCase: AddNewCatalogToTheBeginningUseCase

    @Before
    fun createUseCase() {
        addNewCatalogToTheBeginningUseCase = AddNewCatalogToTheBeginningUseCase(localRepository)
    }

    @Test
    fun addNewCatalogToTheBeginning_catalogAddedPositionsUpdated() {
        val catalog1 = createFakeCatalog("1")
        localRepository.addCatalog(catalog1)
        val addedCatalogs = localRepository.getCatalogs().test().values()[0]

        val catalog2 = createFakeCatalog("2")
        addNewCatalogToTheBeginningUseCase.addNewCatalogToTheBeginning(catalog2, addedCatalogs)
        val resultList = localRepository.getCatalogs().test().values()[0]

        assertThat(resultList.size, `is`(2))

        assertThat(resultList[0].name, `is`("2"))
        assertThat(resultList[0].position, `is`(0))

        assertThat(resultList[1].name, `is`("1"))
        assertThat(resultList[1].position, `is`(1))
    }
}