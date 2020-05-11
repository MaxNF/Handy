package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.PendingRemovedObject

class RemoveGroupUseCaseTest : UseCasesTestBase() {

    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var removeGroupUseCase: RemoveGroupUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject =
            PendingRemovedObject()
        removeGroupUseCase = RemoveGroupUseCase(pendingRemovedObject, localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addGroup(createFakeGroup(name = "2", catalogId = 1, position = 2))
        localRepository.addGroup(createFakeGroup(name = "3", catalogId = 1, position = 3))
    }

    @Test
    fun removeGroupTest_removeMiddleGroup_groupRemovedPositionsChanged() {
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val groupToRemove = addedGroups[2]
        removeGroupUseCase.removeGroup(groupToRemove, addedGroups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups[1].name, `is`("1"))
        assertThat(resultGroups[1].position, `is`(1))
        assertThat(resultGroups[2].name, `is`("3"))
        assertThat(resultGroups[2].position, `is`(2))
        assertThat(pendingRemovedObject.entity as Group, `is`(groupToRemove))
    }
}