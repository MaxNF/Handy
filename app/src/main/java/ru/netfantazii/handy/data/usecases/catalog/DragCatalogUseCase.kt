package ru.netfantazii.handy.data.usecases.catalog

import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.utils.extensions.moveAndReassignPositions
import ru.netfantazii.handy.utils.extensions.sliceModified
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class DragCatalogUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Перемещает каталог с одной позиции на другую. Позиции других каталогов (поле position)
     * изменяются соответственно.
     * */
    fun dragCatalog(catalogList: MutableList<Catalog>, fromPosition: Int, toPosition: Int) {
        catalogList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllCatalogs(catalogList.sliceModified(fromPosition, toPosition))
    }

}