package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class RealRemovePendingCatalogUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val pendingRemovedObject: PendingRemovedObject
) {

    /**
     * Удаляет каталог ожидающий удаления из бд без возможности восстановления. Возвращает true,
     * если объект ожидающий на удаления не был null. Иначе false. Обнуляет переменную внутри
     * pendingRemovedObject если она была не null (содержала каталог).
     * */
    fun realRemovePendingCatalog(catalogList: MutableList<Catalog>): Boolean {
        pendingRemovedObject.entity?.let {
            val catalog = it as Catalog
            catalogList.remove(catalog)
            catalogList.reassignPositions()
            localRepository.removeAndUpdateCatalogs(catalog, catalogList)
            pendingRemovedObject.clearEntity(false)
            return true
        }
        return false
    }
}