package ru.netfantazii.handy.di.modules.catalogs

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.ui.catalogs.CatalogsViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class CatalogsBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(CatalogsViewModel::class)
    abstract fun bindCatalogsViewModel(viewModel: CatalogsViewModel): ViewModel

    @Binds
    abstract fun bindItemAnimator(swipeDismissItemAnimator: DraggableItemAnimator): RecyclerView.ItemAnimator
}