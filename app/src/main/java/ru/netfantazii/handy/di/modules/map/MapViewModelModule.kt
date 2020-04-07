package ru.netfantazii.handy.di.modules.map

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.notifications.map.MapViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class MapViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun bindAlarmViewModel(viewModel: MapViewModel): ViewModel
}