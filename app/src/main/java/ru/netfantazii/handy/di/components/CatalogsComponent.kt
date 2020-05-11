package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.ui.catalogs.CatalogsFragment
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.modules.RecyclerViewProvideModule
import ru.netfantazii.handy.di.modules.catalogs.CatalogsBindModule
import ru.netfantazii.handy.di.modules.catalogs.CatalogsProvideModule

@FragmentScope
@Subcomponent(modules = [CatalogsProvideModule::class, CatalogsBindModule::class, RecyclerViewProvideModule::class])
interface CatalogsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(catalogsFragmentModule: CatalogsProvideModule): CatalogsComponent
    }

    fun inject(catalogsFragment: CatalogsFragment)
}