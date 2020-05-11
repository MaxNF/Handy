package ru.netfantazii.handy.di.components

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import dagger.BindsInstance
import dagger.Subcomponent
import ru.netfantazii.handy.ui.groupsandproducts.GroupsAndProductsFragment
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.modules.RecyclerViewProvideModule
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsProvideModule
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsBindModule

@FragmentScope
@Subcomponent(modules = [GroupsAndProductsBindModule::class, GroupsAndProductsProvideModule::class, RecyclerViewProvideModule::class])
interface GroupsAndProductsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance @CatalogId currentCatalogId: Long, @BindsInstance expandStates: RecyclerViewExpandableItemManager.SavedState, groupsAndProductsProvideModule: GroupsAndProductsProvideModule): GroupsAndProductsComponent
    }

    fun inject(groupsAndProductsFragment: GroupsAndProductsFragment)
}