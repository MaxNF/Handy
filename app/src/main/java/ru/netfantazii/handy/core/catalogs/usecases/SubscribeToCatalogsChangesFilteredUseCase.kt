package ru.netfantazii.handy.core.catalogs.usecases

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class SubscribeToCatalogsChangesFilteredUseCase @Inject constructor(
    private val localRepository: LocalRepository,
    private val pendingRemovedObject: PendingRemovedObject
) {

    /**
     * Подписывает на изменения в базе данных. Список выходящих каталогов фильтруется.
     * Результат не содержит каталог помеченный на удаление (который содержится в pendingRemovedObject)
     * */
    fun subscribeToCatalogsChanges(): Observable<List<Catalog>> =
        localRepository.getCatalogs()
            .observeOn(AndroidSchedulers.mainThread())
            .map { catalogList ->
                catalogList.filterNot { catalog ->
                    catalog == (pendingRemovedObject.entity as Catalog)
                }
            }
}