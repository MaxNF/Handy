package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.utils.extensions.moveAndReassignPositions
import ru.netfantazii.handy.utils.extensions.sliceModified
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.usecases.group.CalculateAndChangeGroupPositionUseCase
import javax.inject.Inject

class ChangeProductStatusUseCase @Inject constructor(
    private val calculateAndChangeGroupPositionUseCase: CalculateAndChangeGroupPositionUseCase,
    private val localRepository: LocalRepository
) {

    fun changeProductStatus(group: Group, product: Product, groupList: MutableList<Group>) {
        updateProductStatusAndPositions(product, group.productList)
        calculateAndChangeGroupPositionUseCase.calculateGroupPosition(group, groupList)
    }

    private fun updateProductStatusAndPositions(
        product: Product,
        productList: MutableList<Product>
    ) {
        val previousStatus = product.buyStatus
        val fromPosition = product.position
        val toPosition: Int
        if (previousStatus == BuyStatus.NOT_BOUGHT) {
            toPosition = productList.lastIndex
            product.buyStatus = BuyStatus.BOUGHT
        } else {
            toPosition = 0
            product.buyStatus = BuyStatus.NOT_BOUGHT
        }
        productList.moveAndReassignPositions(fromPosition, toPosition)
        val productListForUpdating = productList.sliceModified(fromPosition,
            toPosition).toMutableList()
        localRepository.updateAllProducts(productListForUpdating)
    }
}