package ru.netfantazii.handy.di.modules.share

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.share.ShareViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class ShareBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShareViewModel::class)
    abstract fun bindShareViewModel(viewModel: ShareViewModel): ViewModel
}