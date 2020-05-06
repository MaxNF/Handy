package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase

class SubscribeToGroupsChangesUseCaseTest : UseCasesTestBase() {

    private lateinit var subscribeToGroupsChangesUseCase: SubscribeToGroupsChangesUseCase

    @Before
    fun createUseCase() {
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(name = "product 1",
            groupId = 2,
            catalogId = 1,
            position = 0))
        subscribeToGroupsChangesUseCase = SubscribeToGroupsChangesUseCase(localRepository, 1)
    }

    @Test
    fun getFilteredAndNotFilteredGroupsTest_groupsReceivedFilteredListWithoutDefaultGroup() {
        val pair = subscribeToGroupsChangesUseCase.filteredAndNotFilteredGroups.getOrAwaitValue()
        val filteredList = pair.first
        val notFilteredList = pair.second

        assertThat(filteredList.size, `is`(1))
        assertThat(filteredList[0].name, `is`("1"))
        assertThat(filteredList[0].productList.size, `is`(1))

        assertThat(notFilteredList.size, `is`(2))
        assertThat(notFilteredList[0].name, `is`("TOP GROUP"))
    }
}