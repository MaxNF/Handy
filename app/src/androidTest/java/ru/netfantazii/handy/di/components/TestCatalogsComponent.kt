package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.modules.RecyclerViewProvideModule
import ru.netfantazii.handy.di.modules.TestCatalogsProvideModule
import ru.netfantazii.handy.di.modules.catalogs.CatalogsBindModule

@FragmentScope
@Subcomponent(modules = [TestCatalogsProvideModule::class, CatalogsBindModule::class, RecyclerViewProvideModule::class])
interface TestCatalogsComponent : CatalogsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(catalogsFragmentModule: TestCatalogsProvideModule): TestCatalogsComponent
    }
}