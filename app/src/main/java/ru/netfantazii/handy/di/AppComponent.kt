package ru.netfantazii.handy.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.di.module.BillingRepositoryModule
import ru.netfantazii.handy.di.module.DatabaseModule
import ru.netfantazii.handy.di.module.LocalRepositoryModule
import ru.netfantazii.handy.di.module.RemoteRepositoryModule
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, LocalRepositoryModule::class, RemoteRepositoryModule::class, BillingRepositoryModule::class])
interface AppComponent {

    fun inject(application: HandyApplication)

    @Component.Factory
    interface Factory {
        fun create(@ApplicationContext @BindsInstance context: Context): AppComponent
    }

}