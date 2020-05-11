package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class AddNewProductToTheBeginningUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun addNewProduct(product: Product, groupList: MutableList<Group>) {
        val productList = groupList.find { it.id == product.groupId }?.productList
            ?: throw UnsupportedOperationException("Group is not found")
        productList.add(0, product)
        productList.reassignPositions()
        productList.removeAt(0)
        localRepository.addAndUpdateProducts(product, productList)
    }
}