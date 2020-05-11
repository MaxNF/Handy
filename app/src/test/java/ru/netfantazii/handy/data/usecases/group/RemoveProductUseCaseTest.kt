package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.UseCasesTestBase
import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.data.usecases.product.RemoveProductUseCase

class RemoveProductUseCaseTest : UseCasesTestBase() {

    private lateinit var pendingRemovedObject: PendingRemovedObject
    private lateinit var removeProductUseCase: RemoveProductUseCase

    @Before
    fun createUseCase() {
        pendingRemovedObject =
            PendingRemovedObject()
        removeProductUseCase =
            RemoveProductUseCase(
                pendingRemovedObject,
                localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(1,
            2,
            BuyStatus.NOT_BOUGHT,
            "bought product 1",
            0))
        localRepository.addProduct(createFakeProduct(1, 2, BuyStatus.BOUGHT, "bought product 2", 1))
        localRepository.addProduct(createFakeProduct(1, 2, BuyStatus.BOUGHT, "bought product 3", 2))
    }

    @Test
    fun removeProductTest_removeNotBoughtProduct_productRemovedPositionsChangedGroupStatusChanged() {
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val productToRemove = addedGroups[1].productList[0]
        removeProductUseCase.removeProduct(addedGroups[1], productToRemove)

        val resultGroup = localRepository.getGroups(1).getOrAwaitValue()[1]
        val resultProducts = resultGroup.productList

        assertThat(resultGroup.buyStatus, `is`(BuyStatus.BOUGHT))
        assertThat(resultProducts.size, `is`(2))
        assertThat(resultProducts[0].name, `is`("bought product 2"))
        assertThat(resultProducts[0].position, `is`(0))
        assertThat(resultProducts[1].name, `is`("bought product 3"))
        assertThat(resultProducts[1].position, `is`(1))
        assertThat(pendingRemovedObject.entity as Product, `is`(productToRemove))
    }
}