package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.extensions.shiftPositionsToRight
import ru.netfantazii.handy.repositories.LocalRepository
import java.lang.UnsupportedOperationException
import java.util.NoSuchElementException
import javax.inject.Inject

class UndoProductRemovalUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val localRepository: LocalRepository
) {
    fun undoProductRemoval(groupList: MutableList<Group>) {
        pendingRemovedObject.entity?.let { product ->
            if (product !is Product) {
                throw UnsupportedOperationException("Object to be restored should be Product!")
            }
            val productList = groupList.find { group -> group.id == product.groupId }?.productList
                ?: throw NoSuchElementException("Group with id#${product.groupId} is not found")

            if (productList.isEmpty()) {
                localRepository.addProduct(product)
            } else {
                productList.add(product.position, product)
                productList.reassignPositions()
                productList.removeAt(product.position)
                localRepository.addAndUpdateProducts(product, productList)
            }
            pendingRemovedObject.clearEntity(false)
        }
    }
}