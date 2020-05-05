package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.createFakeCatalog
import ru.netfantazii.handy.createFakeGroup
import ru.netfantazii.handy.createFakeTopGroup
import ru.netfantazii.handy.getOrAwaitValue

class DragGroupUseCaseTest : UseCasesTestBase() {

    private lateinit var dragGroupUseCase: DragGroupUseCase

    @Before
    fun createUseCase() {
        dragGroupUseCase =
            DragGroupUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addGroup(createFakeGroup(name = "2", catalogId = 1, position = 2))
        localRepository.addGroup(createFakeGroup(name = "3", catalogId = 1, position = 3))
    }

    @Test
    fun dragGroupTest_dragGroupFromFirstToSecondPosition_allPositionsChanged() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        dragGroupUseCase.dragGroup(1, 2, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()

        assertThat(resultGroups[1].name, `is`("2"))
        assertThat(resultGroups[1].position, `is`(1))

        assertThat(resultGroups[2].name, `is`("1"))
        assertThat(resultGroups[2].position, `is`(2))

        assertThat(resultGroups[3].name, `is`("3"))
        assertThat(resultGroups[3].position, `is`(3))
    }
}