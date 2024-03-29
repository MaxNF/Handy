package ru.netfantazii.handy.di.modules.network

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.ui.main.NetworkViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class NetworkBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(NetworkViewModel::class)
    abstract fun bindNetworkViewModel(viewModel: NetworkViewModel): ViewModel
}