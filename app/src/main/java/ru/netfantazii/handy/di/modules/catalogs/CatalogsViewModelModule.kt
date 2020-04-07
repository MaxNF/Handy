package ru.netfantazii.handy.di.modules.catalogs

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.catalogs.CatalogClickHandler
import ru.netfantazii.handy.core.catalogs.CatalogStorage
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class CatalogsBindModule {

//    @Binds
//    @IntoMap
//    @ViewModelKey(CatalogsViewModel::class)
//    abstract fun bindCatalogsViewModel(viewModel: CatalogsViewModel): ViewModel

    @Binds
    abstract fun bindCatalogStorage(viewModel: CatalogsViewModel): CatalogStorage

    @Binds
    abstract fun bindCatalogClickHandler(viewModel: CatalogsViewModel): CatalogClickHandler
}