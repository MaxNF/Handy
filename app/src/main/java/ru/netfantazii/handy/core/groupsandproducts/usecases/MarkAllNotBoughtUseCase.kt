package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class MarkAllNotBoughtUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun markAllNotBought(groups: List<Group>) {
        val allProducts = groups.flatMap { it.productList }
        allProducts.forEach { it.buyStatus = BuyStatus.NOT_BOUGHT }
        localRepository.updateAllProducts(allProducts)
    }
}