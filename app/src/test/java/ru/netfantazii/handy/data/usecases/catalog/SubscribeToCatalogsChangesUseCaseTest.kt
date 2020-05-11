package ru.netfantazii.handy.data.usecases.catalog

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.getOrAwaitValue

class SubscribeToCatalogsChangesUseCaseTest : UseCasesTestBase() {

    private lateinit var subscribeToCatalogsChangesUseCase: SubscribeToCatalogsChangesUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject

    @Before
    fun createUseCase() {
        pendingRemovedObject =
            PendingRemovedObject()
        subscribeToCatalogsChangesUseCase =
            SubscribeToCatalogsChangesUseCase(localRepository, pendingRemovedObject)
    }

    @Before
    fun populateRepository() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        val catalog3 = createFakeCatalog("3", position = 2)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        localRepository.addCatalog(catalog3)
        pendingRemovedObject.insertEntity(catalog2, false)
    }

    @Test
    fun subscribeToCatalogsChanges_catalogsReceivedAndCorrectlyFiltered() {
        val filteredList =
            subscribeToCatalogsChangesUseCase.filteredAndNotFilteredCatalogs.getOrAwaitValue()
                .first
        val notFilteredList =
            subscribeToCatalogsChangesUseCase.filteredAndNotFilteredCatalogs.getOrAwaitValue()
                .second
        assertThat(filteredList.size, `is`(2))
        assertThat(filteredList[0].name, `is`("1"))
        assertThat(filteredList[1].name, `is`("3"))

        assertThat(notFilteredList.size, `is`(3))
        assertThat(notFilteredList[0].name, `is`("1"))
        assertThat(notFilteredList[1].name, `is`("2"))
        assertThat(notFilteredList[2].name, `is`("3"))
    }
}