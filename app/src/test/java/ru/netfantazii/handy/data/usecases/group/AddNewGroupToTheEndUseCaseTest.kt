package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.BuyStatus

class AddNewGroupToTheEndUseCaseTest : UseCasesTestBase() {

    private lateinit var addNewGroupToTheEndUseCase: AddNewGroupToTheEndUseCase

    @Before
    fun createUseCase() {
        addNewGroupToTheEndUseCase = AddNewGroupToTheEndUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
    }

    @Test
    fun addNewGroup_groupAddedToTheEnd() {
        val newGroup = createFakeGroup(1, name = "new group")
        val groups = localRepository.getGroups(1).getOrAwaitValue()

        addNewGroupToTheEndUseCase.addNewGroup(newGroup, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups.size, `is`(3))
        assertThat(resultGroups[2].name, `is`("new group"))
    }

    @Test
    fun addNewGroup_groupAddedRightBeforeBoughtGroup() {
        localRepository.addGroup(createFakeGroup(1, "bought group", 2))
        localRepository.addProduct(createFakeProduct(1, 3, BuyStatus.BOUGHT))

        val newGroup = createFakeGroup(1, name = "new group")
        val groups = localRepository.getGroups(1).getOrAwaitValue()

        addNewGroupToTheEndUseCase.addNewGroup(newGroup, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups.size, `is`(4))
        assertThat(resultGroups[2].name, `is`("new group"))
        assertThat(resultGroups[3].name, `is`("bought group"))
    }
}