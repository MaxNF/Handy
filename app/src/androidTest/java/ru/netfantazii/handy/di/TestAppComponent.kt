package ru.netfantazii.handy.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.netfantazii.handy.di.repository.TestBillingRepositoryModule
import ru.netfantazii.handy.di.repository.TestDatabaseModule
import ru.netfantazii.handy.di.repository.TestLocalRepositoryModule
import ru.netfantazii.handy.di.repository.TestRemoteRepositoryModule

@Component(modules = [TestDatabaseModule::class, TestLocalRepositoryModule::class, TestBillingRepositoryModule::class, TestRemoteRepositoryModule::class])
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance @ApplicationContext context: Context, @PackageName @BindsInstance packageName: String): TestAppComponent
    }
}