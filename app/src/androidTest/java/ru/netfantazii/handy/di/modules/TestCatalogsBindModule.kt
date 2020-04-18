package ru.netfantazii.handy.di.modules

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.di.ViewModelKey
import ru.netfantazii.handy.di.modules.catalogs.CatalogsBindModule

@Module
abstract class TestCatalogsBindModule {

//    @Binds
//    @IntoMap
//    @ViewModelKey(NetworkViewModel::class)
//    abstract fun bindCatalogsViewModel(viewModel: NetworkViewModel): ViewModel
}