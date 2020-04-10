package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RemoveCatalogUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Удаляет каталог из базы данных. Позиции (поле position) других каталогов меняется
     * соответственно.
     * */
    fun removeCatalog(catalog: Catalog, catalogList: MutableList<Catalog>) {
        catalogList.remove(catalog)
        catalogList.reassignPositions()
        localRepository.removeAndUpdateCatalogs(catalog, catalogList)
    }
}