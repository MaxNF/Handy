package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.data.BuyStatus

class RemoveAllGroupsUseCaseTest : UseCasesTestBase() {

    private lateinit var removeAllGroupsUseCase: RemoveAllGroupsUseCase

    @Before
    fun createUseCase() {
        removeAllGroupsUseCase = RemoveAllGroupsUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addProduct(createFakeProduct(1,
            groupId = 1,
            name = "1",
            buyStatus = BuyStatus.BOUGHT))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(1,
            groupId = 2,
            name = "2",
            buyStatus = BuyStatus.NOT_BOUGHT))
    }

    @Test
    fun removeAllTest_productsAndNotDefaultGroupsAreRemoved() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()

        removeAllGroupsUseCase.removeAll(groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        //должна остаться только пустая дефолтная группа
        assertThat(resultGroups.size, `is`(1))
        assertThat(resultGroups[0].productList.size, `is`(0))
    }
}