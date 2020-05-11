package ru.netfantazii.handy.data.usecases.group

import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netfantazii.handy.*
import ru.netfantazii.handy.data.usecases.product.RenameProductUseCase
import ru.netfantazii.handy.data.usecases.UseCasesTestBase

class RenameProductUseCaseTest : UseCasesTestBase() {

    private lateinit var renameProductUseCase: RenameProductUseCase

    @Before
    fun createUseCase() {
        renameProductUseCase =
            RenameProductUseCase(
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
    fun renameProduct_productRenamed() {
        val addedGroups = localRepository.getGroups(1).getOrAwaitValue()
        val product = addedGroups[1].productList[0]

        product.name = "renamed product"
        renameProductUseCase.renameProduct(product)

        val resultGroups = localRepository.getGroups(1).getOrAwaitValue()
        val resultProduct = resultGroups[1].productList[0]
        assertThat(resultProduct.name, `is`("renamed product"))
    }
}