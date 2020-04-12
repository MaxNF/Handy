package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
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