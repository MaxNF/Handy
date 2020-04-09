package ru.netfantazii.handy.di.modules.groupsandproducts

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.core.groupsandproducts.GroupsAndProductsViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class GroupsAndProductsBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(GroupsAndProductsViewModel::class)
    abstract fun bindGroupsAndProductsViewModel(viewModel: GroupsAndProductsViewModel): ViewModel

    @Binds
    abstract fun bindItemAnimator(swipeDismissItemAnimator: SwipeDismissItemAnimator): RecyclerView.ItemAnimator
}