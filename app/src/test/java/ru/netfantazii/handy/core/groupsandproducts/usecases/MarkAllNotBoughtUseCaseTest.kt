package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.data.BuyStatus

class MarkAllNotBoughtUseCaseTest : UseCasesTestBase() {

    private lateinit var markAllNotBoughtUseCase: MarkAllNotBoughtUseCase

    @Before
    fun createUseCase() {
        markAllNotBoughtUseCase = MarkAllNotBoughtUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addProduct(createFakeProduct(1, groupId = 1, name = "1", buyStatus = BuyStatus.BOUGHT))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(1, groupId = 2, name = "2", buyStatus = BuyStatus.BOUGHT))
    }

    @Test
    fun markAllNotBoughtTest_allGroupsAndProductsAreNotBought() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(groups[0].buyStatus, `is`(BuyStatus.BOUGHT))
        assertThat(groups[1].buyStatus, `is`(BuyStatus.BOUGHT))

        markAllNotBoughtUseCase.markAllNotBought(groups)
        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups[0].buyStatus, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(resultGroups[1].buyStatus, `is`(BuyStatus.NOT_BOUGHT))
    }
}