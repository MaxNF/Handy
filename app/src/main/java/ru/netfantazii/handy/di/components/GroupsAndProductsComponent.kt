package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.groupsandproducts.GroupsAndProductsFragment
import ru.netfantazii.handy.di.viewmodelsmodules.GroupsAndProductsViewModelModule

@Subcomponent(modules = [GroupsAndProductsViewModelModule::class])
interface GroupsAndProductsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): GroupsAndProductsComponent
    }

    fun inject(groupsAndProductsFragment: GroupsAndProductsFragment)
}