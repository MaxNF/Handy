package ru.netfantazii.handy.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.mockito.Mockito
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.di.components.CatalogsComponent
import ru.netfantazii.handy.di.components.TestCatalogsComponent
import ru.netfantazii.handy.di.modules.repository.DatabaseModule
import ru.netfantazii.handy.di.repository.TestBillingRepositoryModule
import ru.netfantazii.handy.di.repository.TestDatabaseModule
import ru.netfantazii.handy.di.repository.TestLocalRepositoryModule
import ru.netfantazii.handy.di.repository.TestRemoteRepositoryModule

@Component(modules = [
    TestAppSubcomponents::class,
    DatabaseModule::class,
    ViewModelBuilder::class,
    TestDatabaseModule::class,
    TestLocalRepositoryModule::class,
    TestBillingRepositoryModule::class,
    TestRemoteRepositoryModule::class])
interface TestAppComponent : AppComponent {

    override fun catalogsComponent(): TestCatalogsComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @ApplicationContext context: Context, @PackageName @BindsInstance packageName: String
        ): TestAppComponent
    }
}