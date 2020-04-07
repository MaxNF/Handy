package ru.netfantazii.handy.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.di.components.*
import ru.netfantazii.handy.di.modules.repository.BillingRepositoryModule
import ru.netfantazii.handy.di.modules.repository.DatabaseModule
import ru.netfantazii.handy.di.modules.repository.LocalRepositoryModule
import ru.netfantazii.handy.di.modules.repository.RemoteRepositoryModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppSubcomponents::class,
    DatabaseModule::class,
    LocalRepositoryModule::class,
    RemoteRepositoryModule::class,
    BillingRepositoryModule::class,
    ViewModelBuilder::class])
interface AppComponent {

    fun inject(application: HandyApplication)
    fun groupsAndProductsComponent(): GroupsAndProductsComponent.Factory
    fun catalogsComponent(): CatalogsComponent.Factory
    fun shareComponent(): ShareComponent.Factory
    fun mainComponent(): MainComponent.Factory
    fun notificationComponent(): NotificationComponent.Factory
    fun alarmComponent(): AlarmComponent.Factory
    fun mapComponent(): MapComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@ApplicationContext @BindsInstance context: Context, @PackageName @BindsInstance packageName: String): AppComponent
    }

}