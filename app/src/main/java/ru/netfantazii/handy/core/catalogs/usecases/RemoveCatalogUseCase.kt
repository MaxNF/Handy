package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RemoveCatalogUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val pendingRemovedObject: PendingRemovedObject
) {

    /**
     * Помещает каталог в очередь на удаление. Первое удаление с момента создания юз кейса не удаляет
     * каталог из бд. Последующие вызовы этого метода заменяют объект находящийся в очереди на новый, а
     * старый объект, который находился в очереди удаляется из базы данных по-настоящему. Позиции
     * (поле position) находящихся в списке объектов меняются соответственным образом.
     * */
    fun removeCatalog(catalog: Catalog, catalogList: MutableList<Catalog>) {
        val catalogForRealRemove = pendingRemovedObject.entity as Catalog?
        pendingRemovedObject.entity = catalog

        catalogForRealRemove?.let {
            realRemove(it, catalogList)
        }
    }

    private fun realRemove(catalog: Catalog, catalogList: MutableList<Catalog>) {
        catalogList.remove(catalog)
        catalogList.reassignPositions()
        localRepository.removeAndUpdateCatalogs(catalog, catalogList)
    }
}