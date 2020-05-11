package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.BuyStatus

class CalculateAndChangeGroupPositionUseCaseTest : UseCasesTestBase() {

    private lateinit var calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase

    @Before
    fun createUseCase() {
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP", position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", position = 1))
        localRepository.addGroup(createFakeGroup(name = "2", position = 2))
        localRepository.addGroup(createFakeGroup(name = "3", position = 3))
        calculateAndChangeGroupPositionUseCase =
            CalculateAndChangeGroupPositionUseCase(localRepository)
    }

    @Test
    fun calculateGroupStatusTest_changeProductStatusFromNonBoughtToBought_groupPositionChangedToLast() {
        // не купленный продукт добавляется в первую не топовую группу
        localRepository.addProduct(createFakeProduct(name = "product 1",
            groupId = 2,
            catalogId = 1,
            buyStatus = BuyStatus.NOT_BOUGHT))
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val product = addedGroups[1].productList[0]
        product.buyStatus = BuyStatus.BOUGHT
        localRepository.updateProduct(product)

        calculateAndChangeGroupPositionUseCase.calculateGroupPosition(addedGroups[1], addedGroups)

        val resultList = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultList[3].name, `is`("1"))
        assertThat(resultList[3].position, `is`(3))
        assertThat(resultList[3].buyStatus, `is`(BuyStatus.BOUGHT))
    }

    @Test
    fun calculateGroupStatusTest_changeProductStatusFromBoughtToNonBought_groupPositionChangedToFirst() {
        // купленный продукт добавляется в третью не топовую группу
        localRepository.addProduct(createFakeProduct(name = "product 1",
            groupId = 4,
            catalogId = 1,
            buyStatus = BuyStatus.BOUGHT))

        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val product = addedGroups[3].productList[0]
        product.buyStatus = BuyStatus.NOT_BOUGHT

        localRepository.updateProduct(product)
        calculateAndChangeGroupPositionUseCase.calculateGroupPosition(addedGroups[3], addedGroups)

        val resultList = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultList[1].name, `is`("3"))
        assertThat(resultList[1].position, `is`(1))
        assertThat(resultList[1].buyStatus, `is`(BuyStatus.NOT_BOUGHT))
    }
}