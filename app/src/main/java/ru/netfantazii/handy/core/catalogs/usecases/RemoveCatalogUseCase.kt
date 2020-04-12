package ru.netfantazii.handy.core.catalogs.usecases

import ru.netfantazii.handy.core.notifications.alarm.usecases.UnregisterAlarmUseCase
import ru.netfantazii.handy.core.notifications.map.usecases.UnregisterAllGeofencesUseCase
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.di.FragmentScope
import javax.inject.Inject

@FragmentScope
class RemoveCatalogUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val unregisterAllGeofencesUseCase: UnregisterAllGeofencesUseCase,
    private val unregisterAlarmUseCase: UnregisterAlarmUseCase,
    private val cancelAssociatedNotificationUseCase: CancelAssociatedNotificationUseCase,
    private val realRemovePendingCatalogUseCase: RealRemovePendingCatalogUseCase
) {

    enum class RemoveCatalogResult {
        REAL_REMOVAL_WAS_NOT_PERFORMED, REAL_REMOVAL_WAS_PERFORMED
    }

    /**
     * Помещает каталог в очередь на удаление. Первое удаление с момента создания юз кейса не удаляет
     * каталог из бд. Последующие вызовы этого метода заменяют объект находящийся в очереди на новый, а
     * старый объект, который находился в очереди удаляется из базы данных по-настоящему. Позиции
     * (поле position) находящихся в списке объектов меняются соответственным образом.
     * */
    fun removeCatalog(
        catalog: Catalog,
        catalogList: MutableList<Catalog>
    ): RemoveCatalogResult {
        cancelAssociatedNotificationUseCase.cancelAssociatedNotifications(catalog.id)
        unregisterAllGeofencesUseCase.unregisterAllGeofences(catalog.id, null)
        unregisterAlarmUseCase.unregisterAlarm(catalog.id)

        val wasReallyRemoved = realRemovePendingCatalogUseCase.realRemovePendingCatalog(catalogList)
        pendingRemovedObject.entity = catalog

        return if (wasReallyRemoved) {
            RemoveCatalogResult.REAL_REMOVAL_WAS_PERFORMED
        } else {
            RemoveCatalogResult.REAL_REMOVAL_WAS_NOT_PERFORMED
        }
    }
}