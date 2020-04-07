package ru.netfantazii.handy.di.modules.alarm

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.notifications.alarm.AlarmViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class AlarmViewModelModule {

//    @Binds
//    @IntoMap
//    @ViewModelKey(AlarmViewModel::class)
//    abstract fun bindAlarmViewModel(viewModel: AlarmViewModel): ViewModel
}