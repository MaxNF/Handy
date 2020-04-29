package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
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