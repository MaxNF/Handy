package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.catalogs.CatalogsFragment
import ru.netfantazii.handy.di.viewmodelsmodules.CatalogsViewModelModule

@Subcomponent(modules = [CatalogsViewModelModule::class])
interface CatalogsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): CatalogsComponent
    }

    fun inject(catalogsFragment: CatalogsFragment)
}