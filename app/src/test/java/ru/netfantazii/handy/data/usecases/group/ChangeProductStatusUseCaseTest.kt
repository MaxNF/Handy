package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.usecases.product.ChangeProductStatusUseCase

class ChangeProductStatusUseCaseTest : UseCasesTestBase() {

    private lateinit var calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase
    private lateinit var changeProductStatusUseCase: ChangeProductStatusUseCase

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
        localRepository.addProduct(createFakeProduct(name = "product 2",
            groupId = 2,
            catalogId = 1,
            position = 1))
        calculateAndChangeGroupPositionUseCase =
            CalculateAndChangeGroupPositionUseCase(localRepository)

        calculateAndChangeGroupPositionUseCase =
            CalculateAndChangeGroupPositionUseCase(localRepository)
        changeProductStatusUseCase =
            ChangeProductStatusUseCase(
                calculateAndChangeGroupPositionUseCase,
                localRepository)
    }

    @Test
    fun changeProductStatusTest_productIsSetBought_PositionChangedToLast() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        val firstProduct = groups[1].productList[0]

        changeProductStatusUseCase.changeProductStatus(groups[1], firstProduct, groups)
        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        val resultProduct = resultGroups[1].productList[1]

        assertThat(resultProduct.name, `is`("product 1"))
        assertThat(resultProduct.buyStatus, `is`(BuyStatus.BOUGHT))
        assertThat(resultProduct.position, `is`(1))
    }

    @Test
    fun changeProductStatusTest_productIsSetNonBought_PositionChangedToFirst() {
        localRepository.addProduct(createFakeProduct(name = "product 3",
            groupId = 2,
            catalogId = 1,
            position = 2,
            buyStatus = BuyStatus.BOUGHT))
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        val lastProduct = groups[1].productList[2]

        changeProductStatusUseCase.changeProductStatus(groups[1], lastProduct, groups)
        val resultProduct = groups[1].productList[0]

        assertThat(resultProduct.name, `is`("product 3"))
        assertThat(resultProduct.buyStatus, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(resultProduct.position, `is`(0))
    }

    @Test
    fun changeProductStatusTest_allProductsAreSetBought_groupStatusChangedToBought() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        val firstProduct = groups[1].productList[0]
        val secondProduct = groups[1].productList[1]

        changeProductStatusUseCase.changeProductStatus(groups[1], firstProduct, groups)
        changeProductStatusUseCase.changeProductStatus(groups[1], secondProduct, groups)
        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()

        assertThat(resultGroups[1].buyStatus, `is`(BuyStatus.BOUGHT))
    }

    @Test
    fun changeProductStatusTest_allProductsAreSetNonBought_groupStatusChangedToNonBought() {
        localRepository.addGroup(createFakeGroup(1, "2", position = 2))
        localRepository.addProduct(createFakeProduct(1, 3, BuyStatus.BOUGHT, "product 1"))
        localRepository.addProduct(createFakeProduct(1, 3, BuyStatus.BOUGHT, "product 2"))

        val groups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(groups[2].buyStatus, `is`(BuyStatus.BOUGHT))

        val firstProduct = groups[2].productList[0]
        val secondProduct = groups[2].productList[1]

        changeProductStatusUseCase.changeProductStatus(groups[2], firstProduct, groups)
        changeProductStatusUseCase.changeProductStatus(groups[2], secondProduct, groups)
        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()

        assertThat(resultGroups[1].name, `is`("2"))
        assertThat(resultGroups[1].buyStatus, `is`(BuyStatus.NOT_BOUGHT))
    }
}