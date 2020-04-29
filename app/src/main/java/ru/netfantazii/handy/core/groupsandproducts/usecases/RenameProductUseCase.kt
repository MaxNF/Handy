package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RenameProductUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun renameProduct(product: Product) {
        localRepository.updateProduct(product)
    }
}