package ru.netfantazii.handy.di.modules.alarm

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.core.notifications.alarm.AlarmViewModel
import ru.netfantazii.handy.core.notifications.alarm.AlarmVmFactory
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.repositories.LocalRepository as LocalRepository1

@Module
class AlarmProvideModule(private val fragment: Fragment) {

    @Provides
    fun provideAlarmViewModel(localRepository: LocalRepository1, @ApplicationContext context: Context) =
        ViewModelProviders.of(fragment,
            AlarmVmFactory((context as Application),
                localRepository)).get(AlarmViewModel::class.java)
}