package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.extensions.moveAndReassignPositions
import ru.netfantazii.handy.extensions.sliceModified
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

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