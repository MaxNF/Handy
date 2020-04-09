package ru.netfantazii.handy.di.modules.groupsandproducts

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.core.catalogs.CatalogsAdapter
import ru.netfantazii.handy.core.groupsandproducts.*
import ru.netfantazii.handy.di.*
import ru.netfantazii.handy.extensions.injectViewModel

@Module
class GroupsAndProductsProvideModule(private val fragment: Fragment) {

    @Provides
    fun provideGroupClickHandler(factory: ViewModelFactory): GroupClickHandler =
        fragment.injectViewModel<GroupsAndProductsViewModel>(factory) as GroupClickHandler

    @Provides
    fun provideProductClickHandler(factory: ViewModelFactory): ProductClickHandler =
        fragment.injectViewModel<GroupsAndProductsViewModel>(factory) as ProductClickHandler

    @Provides
    fun provideGroupStorage(factory: ViewModelFactory): GroupStorage =
        fragment.injectViewModel<GroupsAndProductsViewModel>(factory) as GroupStorage

    @Provides
    @FragmentScope
    fun provideExpandManager() =
        RecyclerViewExpandableItemManager(null).apply {
            defaultGroupsExpandedState = true
        }

    @FragmentScope
    @Provides
    @WrappedAdapter
    fun provideWrappedAdapter(
        expandManager: RecyclerViewExpandableItemManager,
        dragManager: RecyclerViewDragDropManager,
        swipeManager: RecyclerViewSwipeManager,
        adapter: GroupsAndProductsAdapter
    ): RecyclerView.Adapter<*> {
        var wrappedAdapter = expandManager.createWrappedAdapter(adapter)
        wrappedAdapter = dragManager.createWrappedAdapter(wrappedAdapter)
        return swipeManager.createWrappedAdapter(wrappedAdapter)
    }

    @FragmentScope
    @Provides
    fun provideItemAnimator() = SwipeDismissItemAnimator().apply { supportsChangeAnimations = false }
}