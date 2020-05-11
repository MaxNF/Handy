package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.usecases.group.CalculateAndChangeGroupPositionUseCase
import java.lang.UnsupportedOperationException
import java.util.NoSuchElementException
import javax.inject.Inject

class UndoProductRemovalUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val localRepository: LocalRepository,
    private val calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase
) {
    fun undoProductRemoval(groupList: MutableList<Group>) {
        pendingRemovedObject.entity?.let { product ->
            if (product !is Product) {
                throw UnsupportedOperationException("Object to be restored should be Product!")
            }
            val group = groupList.find { group -> group.id == product.groupId }
            val productList = group?.productList
                ?: throw NoSuchElementException("Group with id#${product.groupId} is not found")

            if (productList.isEmpty()) {
                localRepository.addProduct(product)
            } else {
                productList.add(product.position, product)
                productList.reassignPositions()
//                productList.removeAt(product.position)
                localRepository.addAndUpdateProducts(product, productList)
            }
            calculateAndChangeGroupPositionUseCase.calculateGroupPosition(group, groupList)
            pendingRemovedObject.clearEntity(false)
        }
    }
}