package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.notifications.alarm.AlarmFragment
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.modules.alarm.AlarmProvideModule
import ru.netfantazii.handy.di.modules.alarm.AlarmViewModelModule

@FragmentScope
@Subcomponent(modules = [AlarmProvideModule::class])
interface AlarmComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(alarmProvideModule: AlarmProvideModule): AlarmComponent
    }

    fun inject(alarmFragment: AlarmFragment)
}