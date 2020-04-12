package ru.netfantazii.handy.core.catalogs.usecases

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class LoadCatalogNetInfoUseCase @Inject constructor(private val localRepository: LocalRepository) {

    /**
     * Подгружает доп. данные о полученном из сети списке покупок из бд.
     * */
    fun fetchCatalogWithNetInfo(catalog: Catalog): Single<CatalogNetInfoEntity> =
        localRepository.getCatalogNetInfo(catalog.id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}