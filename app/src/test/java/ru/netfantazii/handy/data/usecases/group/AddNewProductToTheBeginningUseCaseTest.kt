package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.product.AddNewProductToTheBeginningUseCase
import ru.netfantazii.handy.data.usecases.UseCasesTestBase

@RunWith(RobolectricTestRunner::class)
class AddNewProductToTheBeginningUseCaseTest : UseCasesTestBase() {

    private lateinit var addNewProductToTheBeginningUseCase: AddNewProductToTheBeginningUseCase

    @Before
    fun createUseCase() {
        addNewProductToTheBeginningUseCase =
            AddNewProductToTheBeginningUseCase(
                localRepository)
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
    fun addNewProductTest_productAddedToTheBeginning() {
        val newProduct = createFakeProduct(1, 2, name = "new product")

        val groups = localRepository.getGroups(1).getOrAwaitValue()
        addNewProductToTheBeginningUseCase.addNewProduct(newProduct, groups)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        val productList = resultGroups[1].productList
        assertThat(productList.size, `is`(2))
        assertThat(productList[0].name, `is`("new product"))
        assertThat(productList[1].name, `is`("product 1"))
    }
}