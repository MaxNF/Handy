package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.groupsandproducts.GroupsAndProductsFragment
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsAdapterModule
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsFragmentModule
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsViewModelModule

@Subcomponent(modules = [GroupsAndProductsViewModelModule::class, GroupsAndProductsAdapterModule::class, GroupsAndProductsFragmentModule::class])
interface GroupsAndProductsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): GroupsAndProductsComponent
    }

    fun inject(groupsAndProductsFragment: GroupsAndProductsFragment)
}