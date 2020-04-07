package ru.netfantazii.handy.di.modules.catalogs

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import ru.netfantazii.handy.core.catalogs.CatalogClickHandler
import ru.netfantazii.handy.core.catalogs.CatalogStorage
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.core.catalogs.CatalogsVmFactory
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Provider

@Module
class CatalogsProvideModule(private val fragment: Fragment) {

    @Provides
    fun provideCatalogsViewModel(localRepository: LocalRepository, @ApplicationContext context: Context) =
        ViewModelProviders.of(fragment,
            CatalogsVmFactory(localRepository,
                context as Application)).get(CatalogsViewModel::class.java)
}