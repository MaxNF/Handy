package ru.netfantazii.handy.data.usecases.catalog

import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class RenameCatalogUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Переименовывает каталог.
     * */
    fun renameCatalog(catalog: Catalog) {
        localRepository.updateCatalog(catalog)
    }
}