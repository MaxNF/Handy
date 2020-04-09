package ru.netfantazii.handy.di.modules.catalogs

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.core.catalogs.CatalogClickHandler
import ru.netfantazii.handy.core.catalogs.CatalogStorage
import ru.netfantazii.handy.core.catalogs.CatalogsAdapter
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.di.*
import ru.netfantazii.handy.extensions.injectViewModel

@Module
class CatalogsProvideModule(private val fragment: Fragment) {

    @Provides
    fun provideCatalogStorage(factory: ViewModelFactory) =
        fragment.injectViewModel<CatalogsViewModel>(factory) as CatalogStorage

    @Provides
    fun provideCatalogClickHandler(factory: ViewModelFactory) =
        fragment.injectViewModel<CatalogsViewModel>(factory) as CatalogClickHandler

    @Provides
    fun provideNetworkViewModel(factory: ViewModelFactory) =
        fragment.activity!!.injectViewModel<NetworkViewModel>(factory)

    @FragmentScope
    @Provides
    @WrappedAdapter
    fun provideWrappedAdapter(
        dragManager: RecyclerViewDragDropManager,
        swipeManager: RecyclerViewSwipeManager,
        adapter: CatalogsAdapter
    ): RecyclerView.Adapter<*> {
        val wrappedAdapter = dragManager.createWrappedAdapter(adapter)
        return swipeManager.createWrappedAdapter(wrappedAdapter)
    }

    @FragmentScope
    @Provides
    fun provideItemAnimator() = DraggableItemAnimator().apply { supportsChangeAnimations = false }
}