package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class MarkAllNotBoughtUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun markAllNotBought(groups: List<Group>) {
        val allProducts = groups.flatMap { it.productList }
        allProducts.forEach { it.buyStatus = BuyStatus.NOT_BOUGHT }
        localRepository.updateAllProducts(allProducts)
    }
}