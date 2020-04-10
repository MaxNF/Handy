package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RenameCatalogUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Переименовывает каталог.
     * */
    fun renameCatalog(catalog: Catalog) {
        localRepository.updateCatalog(catalog)
    }
}