package ru.netfantazii.handy.data.usecases.catalog

import ru.netfantazii.handy.data.usecases.alarm.UnregisterAlarmUseCase
import ru.netfantazii.handy.data.usecases.map.UnregisterAllGeofencesUseCase
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.model.PendingRemovedObject
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
    /**
     * Помещает каталог в очередь на удаление. Первое удаление с момента создания юз кейса не удаляет
     * каталог из бд. Последующие вызовы этого метода заменяют объект находящийся в очереди на новый, а
     * старый объект, который находился в очереди удаляется из базы данных по-настоящему. Позиции
     * (поле position) находящихся в списке объектов меняются соответственным образом.
     * */
    fun removeCatalog(
        catalog: Catalog,
        catalogList: MutableList<Catalog>
    ) {
        cancelAssociatedNotificationUseCase.cancelAssociatedNotifications(catalog.id)
        unregisterAllGeofencesUseCase.unregisterAllGeofences(catalog.id, null)
        unregisterAlarmUseCase.unregisterAlarm(catalog.id)

        val wasReallyRemoved = realRemovePendingCatalogUseCase.realRemovePendingCatalog(catalogList)
        pendingRemovedObject.insertEntity(catalog, !wasReallyRemoved)
    }
}