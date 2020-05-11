package ru.netfantazii.handy.data.usecases.product

import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class RenameProductUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun renameProduct(product: Product) {
        localRepository.updateProduct(product)
    }
}