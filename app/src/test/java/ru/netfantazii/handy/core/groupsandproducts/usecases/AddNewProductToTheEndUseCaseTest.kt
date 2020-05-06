package ru.netfantazii.handy.core.groupsandproducts.usecases

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.UseCasesTestBase
import ru.netfantazii.handy.data.BuyStatus

@RunWith(RobolectricTestRunner::class)
class AddNewProductToTheEndUseCaseTest : UseCasesTestBase() {

    private lateinit var addNewProductToTheEndUseCase: AddNewProductToTheEndUseCase

    @Before
    fun createUseCase() {
        addNewProductToTheEndUseCase = AddNewProductToTheEndUseCase(localRepository)
        localRepository.addCatalog(createFakeCatalog())
        localRepository.addGroup(createFakeTopGroup(name = "TOP GROUP",
            catalogId = 1,
            position = 0))
        localRepository.addGroup(createFakeGroup(name = "1", catalogId = 1, position = 1))
        localRepository.addProduct(createFakeProduct(name = "product 1",
            groupId = 2,
            catalogId = 1,
            position = 0))
    }

    @Test
    fun addNewProductTest_productAddedToTheEnd() {
        val newProduct = createFakeProduct(1, 2, name = "new product")

        val groups = localRepository.getGroups(1).getOrAwaitValue()
        addNewProductToTheEndUseCase.addNewProduct(newProduct, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        val productList = resultGroups[1].productList

        assertThat(productList.size, `is`(2))

        assertThat(productList[0].name, `is`("product 1"))
        assertThat(productList[1].name, `is`("new product"))
    }

    @Test
    fun addNewProductTest_productAddedBeforeFirstBoughtProduct() {
        localRepository.addProduct(createFakeProduct(1, 2, BuyStatus.BOUGHT, "bought product", 1))
        val newProduct = createFakeProduct(1, 2, name = "new product")

        val groups = localRepository.getGroups(1).getOrAwaitValue()
        addNewProductToTheEndUseCase.addNewProduct(newProduct, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        val productList = resultGroups[1].productList
        assertThat(productList.size, `is`(3))
        assertThat(productList[0].name, `is`("product 1"))
        assertThat(productList[1].name, `is`("new product"))
        assertThat(productList[2].name, `is`("bought product"))

    }
}