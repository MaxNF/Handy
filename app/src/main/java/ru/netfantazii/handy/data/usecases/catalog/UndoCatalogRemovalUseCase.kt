package ru.netfantazii.handy.data.usecases.catalog

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.usecases.alarm.RegisterAlarmUseCase
import ru.netfantazii.handy.data.usecases.map.RegisterGeofencesUseCase
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class UndoCatalogRemovalUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val pendingRemovedObject: PendingRemovedObject,
    private val registerGeofencesUseCase: RegisterGeofencesUseCase,
    private val registerAlarmUseCase: RegisterAlarmUseCase
) {

    /**
     * Отменяет удаление каталога. Фактически просто очищает pendingRemovedObject, чтобы объекты не
     * фильтровались. Также заново регистрирует напоминания (геозоны и по-времени).
     * */
    fun undoRemoval() =
        pendingRemovedObject.entity?.let { entity ->
            val catalog = entity as Catalog
            localRepository.getGeofences(catalog.id)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable { geofenceEntities ->
                    registerGeofencesUseCase.registerGeofences(
                        geofenceEntities,
                        catalog.id,
                        catalog.name,
                        catalog.groupExpandStates)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                }
                .doFinally {
                    catalog.alarmTime?.let { alarmTime ->
                        registerAlarmUseCase.registerAlarm(
                            catalog.id,
                            catalog.name,
                            catalog.groupExpandStates,
                            alarmTime)
                    }
                    pendingRemovedObject.clearEntity(true)
                }
        }
}