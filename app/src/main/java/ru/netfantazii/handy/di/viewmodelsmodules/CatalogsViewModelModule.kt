package ru.netfantazii.handy.di.viewmodelsmodules

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class CatalogsViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(CatalogsViewModel::class)
    abstract fun bindCatalogsViewModel(viewModel: CatalogsViewModel): ViewModel
}