package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.product.DragProductUseCase
import ru.netfantazii.handy.data.usecases.UseCasesTestBase

class DragProductUseCaseTest : UseCasesTestBase() {

    private lateinit var dragProductUseCase: DragProductUseCase

    @Before
    fun createUseCase() {
        val calculateAndChangeGroupPositionUseCase =
            Mockito.mock(CalculateAndChangeGroupPositionUseCase::class.java)
        dragProductUseCase =
            DragProductUseCase(
                localRepository,
                calculateAndChangeGroupPositionUseCase)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
    }

    @Test
    fun dragProductTest_dragProductInTheSameGroup_positionChanged() {
        localRepository.addProduct(createFakeProduct(name = "Drag Product", position = 0))
        localRepository.addProduct(createFakeProduct(name = "product 1", position = 1))

        val groups = localRepository.getGroups(1).getOrAwaitValue()

        dragProductUseCase.dragProduct(groups[0], 0, groups[0], 1, groups)

        val resultProducts = localRepository.getGroups(1).getOrAwaitValue()[0].productList
        assertThat(resultProducts[0].name, `is`("product 1"))
        assertThat(resultProducts[0].position, `is`(0))

        assertThat(resultProducts[1].name, `is`("Drag Product"))
        assertThat(resultProducts[1].position, `is`(1))
    }

    @Test
    fun dragProductTest_dragProductToAnotherGroup_positionChanged() {
        localRepository.addProduct(createFakeProduct(name = "Drag Product", position = 0))
        localRepository.addProduct(createFakeProduct(name = "product 1", position = 1))

        val groups = localRepository.getGroups(1).getOrAwaitValue()

        dragProductUseCase.dragProduct(groups[0], 0, groups[1], 0, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()

        assertThat(resultGroups[0].productList.size, `is`(1))
        assertThat(resultGroups[1].productList.size, `is`(1))

        val firstGroupProducts = resultGroups[0].productList
        val secondGroupProducts = resultGroups[1].productList

        assertThat(firstGroupProducts[0].name, `is`("product 1"))
        assertThat(firstGroupProducts[0].position, `is`(0))

        assertThat(secondGroupProducts[0].name, `is`("Drag Product"))
        assertThat(secondGroupProducts[0].position, `is`(0))
    }
}