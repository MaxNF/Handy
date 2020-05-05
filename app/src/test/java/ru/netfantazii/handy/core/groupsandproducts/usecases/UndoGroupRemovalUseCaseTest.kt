package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.data.PendingRemovedObject

class UndoGroupRemovalUseCaseTest : UseCasesTestBase() {

    private lateinit var undoGroupRemovalUseCase: UndoGroupRemovalUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        undoGroupRemovalUseCase = UndoGroupRemovalUseCase(pendingRemovedObject, localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addGroup(createFakeGroup(name = "2", catalogId = 1, position = 2))
    }

    @Test
    fun undoGroupRemovalTest_undoGroupRemoval_GroupRestoredWithAllProducts() {
        val productsToRestore = mutableListOf(
            createFakeProduct(1, 4, position = 0),
            createFakeProduct(1, 4, position = 1)
        )
        val groupToRestore =
            createFakeGroup(1, "restored group", 2, productList = productsToRestore)
        pendingRemovedObject.insertEntity(groupToRestore, false)
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()

        undoGroupRemovalUseCase.undoGroupRemoval(addedGroups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups.size, `is`(4))
        assertThat(resultGroups[2].name, `is`("restored group"))
        assertThat(resultGroups[2].position, `is`(2))
        assertThat(resultGroups[2].productList.size, `is`(2))
        assertThat(resultGroups[3].name, `is`("2"))
        assertThat(resultGroups[3].position, `is`(3))
        assertThat(pendingRemovedObject.entity, `is`(CoreMatchers.nullValue()))
    }
}