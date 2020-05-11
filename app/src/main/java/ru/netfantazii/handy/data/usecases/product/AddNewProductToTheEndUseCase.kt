package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class AddNewProductToTheEndUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun addNewProduct(product: Product, groupList: MutableList<Group>) {
        val productList = groupList.find { it.id == product.groupId }?.productList
            ?: throw UnsupportedOperationException("Group is not found")
        val newProductPosition = getPositionForInsertion(productList)
        productList.add(newProductPosition, product)
        productList.reassignPositions()
        productList.removeAt(newProductPosition)
        localRepository.addAndUpdateProducts(product, productList)
    }

    private fun getPositionForInsertion(productList: List<Product>): Int {
        val lastNotBoughtProduct = productList.findLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
        return if (lastNotBoughtProduct != null) lastNotBoughtProduct.position + 1 else 0
    }

}