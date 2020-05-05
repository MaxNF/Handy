package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.extensions.moveAndReassignPositions
import ru.netfantazii.handy.extensions.moveBetweenListsAndReassignPositions
import ru.netfantazii.handy.extensions.sliceModified
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class DragProductUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase
) {
    fun dragProduct(
        fromGroup: Group,
        fromPosition: Int,
        toGroup: Group,
        toPosition: Int,
        groupList: MutableList<Group>
    ) {
        val firstProductList = fromGroup.productList
        val secondProductList = toGroup.productList
        if (firstProductList == secondProductList) {
            firstProductList.moveAndReassignPositions(fromPosition, toPosition)
            localRepository.updateAllProducts(firstProductList.sliceModified(fromPosition,
                toPosition))
        } else {
            firstProductList[fromPosition].groupId = toGroup.id
            moveBetweenListsAndReassignPositions(firstProductList,
                fromPosition,
                secondProductList,
                toPosition)
            val listForUpdating = mutableListOf<Product>()
            with(listForUpdating) {
                addAll(firstProductList)
                addAll(secondProductList)
            }
            localRepository.updateAllProducts(listForUpdating)
            calculateAndChangeGroupPositionUseCase.calculateGroupPosition(fromGroup, groupList)
            calculateAndChangeGroupPositionUseCase.calculateGroupPosition(toGroup, groupList)
        }
    }
}