package ru.netfantazii.handy.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.di.components.CatalogsComponent
import ru.netfantazii.handy.di.components.GroupsAndProductsComponent
import ru.netfantazii.handy.di.module.*
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

    @Component.Factory
    interface Factory {
        fun create(@ApplicationContext @BindsInstance context: Context): AppComponent
    }

}