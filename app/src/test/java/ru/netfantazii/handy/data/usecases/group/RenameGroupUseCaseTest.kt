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

class RenameGroupUseCaseTest : UseCasesTestBase() {

    private lateinit var renameGroupUseCase: RenameGroupUseCase

    @Before
    fun createUseCase() {
        renameGroupUseCase = RenameGroupUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
    }

    @Test
    fun renameGroup_groupRenamed() {
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val groupToRename = addedGroups[1]
        groupToRename.name = "renamed group"

        renameGroupUseCase.renameGroup(groupToRename)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups[1].name, `is`("renamed group"))
    }
}