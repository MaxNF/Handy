package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.data.PendingRemovedObject

class UndoProductRemovalUseCaseTest : UseCasesTestBase() {

    private lateinit var undoProductRemovalUseCase: UndoProductRemovalUseCase
    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject = PendingRemovedObject()
        calculateAndChangeGroupPositionUseCase =
            CalculateAndChangeGroupPositionUseCase(localRepository)
        undoProductRemovalUseCase = UndoProductRemovalUseCase(pendingRemovedObject,
            localRepository,
            calculateAndChangeGroupPositionUseCase)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(name = "product 1",
            groupId = 2,
            catalogId = 1,
            position = 0))
        localRepository.addProduct(createFakeProduct(name = "product 2",
            groupId = 2,
            catalogId = 1,
            position = 1))
    }

    @Test
    fun undoProductRemovalTest_undoProductRemoval_ProductRemovedToTheSecondPosition() {
        val productForRestore = createFakeProduct(1, 2, name = "restored product", position = 1)
        pendingRemovedObject.insertEntity(productForRestore, false)
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()

        undoProductRemovalUseCase.undoProductRemoval(addedGroups)

        val resultGroup = localRepository.getGroups(1).getOrAwaitValue()[1]
        val resultProducts = resultGroup.productList
        assertThat(resultProducts.size, `is`(3))
        assertThat(resultProducts[0].name, `is`("product 1"))
        assertThat(resultProducts[0].position, `is`(0))
        assertThat(resultProducts[1].name, `is`("restored product"))
        assertThat(resultProducts[1].position, `is`(1))
        assertThat(resultProducts[2].name, `is`("product 2"))
        assertThat(resultProducts[2].position, `is`(2))
        assertThat(pendingRemovedObject.entity, `is`(nullValue()))
    }
}