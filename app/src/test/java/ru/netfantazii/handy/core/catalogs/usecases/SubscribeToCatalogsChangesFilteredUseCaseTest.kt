package ru.netfantazii.handy.core.catalogs.usecases

import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import ru.netfantazii.handy.RxImmediateSchedulerRule
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.data.PendingRemovedObject

class SubscribeToCatalogsChangesFilteredUseCaseTest : CatalogUseCasesTestBase() {

    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    private lateinit var subscribeToCatalogsChangesFilteredUseCase: SubscribeToCatalogsChangesFilteredUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        subscribeToCatalogsChangesFilteredUseCase =
            SubscribeToCatalogsChangesFilteredUseCase(localRepository, pendingRemovedObject)
    }

    @Before
    fun populateRepository() {
        val catalog1 = createFakeCatalog("1", position = 0)
        val catalog2 = createFakeCatalog("2", position = 1)
        val catalog3 = createFakeCatalog("3", position = 2)
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        localRepository.addCatalog(catalog3)
        pendingRemovedObject.entity = catalog2
    }

    @Test
    fun subscribeToCatalogsChanges_catalogsReceivedAndCorrectlyFiltered() {
        val resultList =
            subscribeToCatalogsChangesFilteredUseCase.subscribeToCatalogsChanges().test().values()[0]
        assertThat(resultList.size, `is`(2))
        assertThat(resultList[0].name, `is`("1"))
        assertThat(resultList[1].name, `is`("3"))
    }
}