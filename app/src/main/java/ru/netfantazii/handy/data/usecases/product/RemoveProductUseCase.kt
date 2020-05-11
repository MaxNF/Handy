package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.shiftPositionsToLeft
import ru.netfantazii.handy.data.repositories.LocalRepository
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