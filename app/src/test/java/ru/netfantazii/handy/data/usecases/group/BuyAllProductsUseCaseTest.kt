package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.usecases.product.BuyAllProductsUseCase

class BuyAllProductsUseCaseTest : UseCasesTestBase() {

    private lateinit var buyAllProductsUseCase: BuyAllProductsUseCase

    @Before
    fun createUseCase() {
        buyAllProductsUseCase =
            BuyAllProductsUseCase(
                localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addProduct(createFakeProduct(1, groupId = 1, name = "1"))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(1, groupId = 2, name = "2"))
    }

    @Test
    fun buyAllTest_allProductsAndGroupsAreBought() {
        val groups = localRepository.getGroups(1).getOrAwaitValue()
        buyAllProductsUseCase.buyAll(groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        assertThat(resultGroups[0].buyStatus, `is`(BuyStatus.BOUGHT))
        assertThat(resultGroups[1].buyStatus, `is`(BuyStatus.BOUGHT))
    }
}