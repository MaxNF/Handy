package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.moveAndReassignPositions
import ru.netfantazii.handy.utils.extensions.moveBetweenListsAndReassignPositions
import ru.netfantazii.handy.utils.extensions.sliceModified
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.usecases.group.CalculateAndChangeGroupPositionUseCase
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