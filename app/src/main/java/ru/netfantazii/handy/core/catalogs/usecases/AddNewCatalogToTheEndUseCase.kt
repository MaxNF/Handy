package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class AddNewCatalogToTheEndUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Добавляет новый каталог (список покупок) в конец списка.
     * Позиции (поле position) других списков не меняются.
     * */
    fun addNewCatalogToTheEnd(catalog: Catalog, catalogList: MutableList<Catalog>) {
        catalog.position = catalogList.size
        localRepository.addCatalog(catalog)
    }
}