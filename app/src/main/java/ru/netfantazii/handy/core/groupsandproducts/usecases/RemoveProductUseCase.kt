package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.extensions.shiftPositionsToLeft
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RemoveProductUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val localRepository: LocalRepository
) {

    fun removeProduct(group: Group, product: Product) {
        val productList = group.productList
        if (productList.size == 1) {
            localRepository.removeProduct(product)
        } else {
            val removedProductIndex = group.productList.indexOf(product)
            val listForUpdating =
                productList.slice((removedProductIndex + 1)..group.productList.lastIndex)
            listForUpdating.shiftPositionsToLeft()
            localRepository.removeAndUpdateProducts(product, listForUpdating)
        }
        pendingRemovedObject.insertEntity(product, false)
    }
}