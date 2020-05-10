package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class BuyAllProductsUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun buyAll(groups: List<Group>) {
        val allProducts = groups.flatMap { it.productList }
        allProducts.forEach { it.buyStatus = BuyStatus.BOUGHT }
        localRepository.updateAllProducts(allProducts)
    }
}