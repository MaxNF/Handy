package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.createFakeGroup
import ru.netfantazii.handy.createFakeTopGroup
import ru.netfantazii.handy.getOrAwaitValue

class AddNewGroupToTheBeginningUseCaseTest : UseCasesTestBase() {

    private lateinit var addNewGroupToTheBeginningUseCase: AddNewGroupToTheBeginningUseCase

    @Before
    fun createUseCase() {
        addNewGroupToTheBeginningUseCase = AddNewGroupToTheBeginningUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
    }

    @Test
    fun addNewGroup_groupAddedRightAfterDefaultGroup() {
        val newGroup = createFakeGroup(1, name = "new group")
        val groups = localRepository.getGroups(1).getOrAwaitValue()

        addNewGroupToTheBeginningUseCase.addNewGroup(newGroup, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups.size, `is`(3))
        assertThat(resultGroups[1].name, `is`("new group"))
    }
}