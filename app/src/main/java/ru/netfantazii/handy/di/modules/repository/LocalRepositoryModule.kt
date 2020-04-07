package ru.netfantazii.handy.di.modules.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.LocalRepositoryImpl

@Module
abstract class LocalRepositoryModule {

    @Binds
    abstract fun bindLocalRepository(localRepo: LocalRepositoryImpl): LocalRepository
}