package ru.netfantazii.handy.data.usecases.catalog

import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class AddNewCatalogToTheBeginningUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Добавляет новый каталог (список покупок) в начало списка.
     * Позиции (поле position) других списков становится равной их новой визуальной позиции
     * (то есть инкрементируется на 1)
     * */
    fun addNewCatalogToTheBeginning(catalog: Catalog, catalogList: MutableList<Catalog>) {
        catalogList.add(0, catalog)
        catalogList.reassignPositions()
        catalogList.removeAt(0)
        localRepository.addAndUpdateCatalogs(catalog, catalogList)
    }
}