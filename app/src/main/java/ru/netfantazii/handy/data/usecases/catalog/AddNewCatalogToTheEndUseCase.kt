package ru.netfantazii.handy.data.usecases.catalog

import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
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