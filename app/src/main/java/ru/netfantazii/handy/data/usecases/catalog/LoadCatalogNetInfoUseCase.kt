package ru.netfantazii.handy.data.usecases.catalog

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.localdb.CatalogNetInfoEntity
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.data.repositories.LocalRepository
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