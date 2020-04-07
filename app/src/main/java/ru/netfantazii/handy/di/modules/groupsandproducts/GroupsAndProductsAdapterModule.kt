package ru.netfantazii.handy.di.modules.groupsandproducts

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.core.groupsandproducts.GroupClickHandler
import ru.netfantazii.handy.core.groupsandproducts.GroupStorage
import ru.netfantazii.handy.core.groupsandproducts.GroupsAndProductsViewModel
import ru.netfantazii.handy.core.groupsandproducts.ProductClickHandler

@Module
abstract class GroupsAndProductsAdapterModule {

    @Binds
    abstract fun bindGroupClickHandler(viewModel: GroupsAndProductsViewModel): GroupClickHandler

    @Binds
    abstract fun bindProductClickHandler(viewModel: GroupsAndProductsViewModel): ProductClickHandler

    @Binds
    abstract fun bindGroupStorage(viewModel: GroupsAndProductsViewModel): GroupStorage
}