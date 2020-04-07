package ru.netfantazii.handy.di.modules.groupsandproducts

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import dagger.Module
import dagger.Provides

@Module
class GroupsAndProductsFragmentModule {

    @Provides
    fun provideExpandManager() =
        RecyclerViewExpandableItemManager(null).apply {
            defaultGroupsExpandedState = true
        }
}