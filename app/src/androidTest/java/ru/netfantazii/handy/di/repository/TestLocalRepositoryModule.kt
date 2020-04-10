package ru.netfantazii.handy.di.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.repositories.LocalRepository

@Module
abstract class TestLocalRepositoryModule {
    @Binds
    abstract fun bindLocalRepository(localRepo: FakeLocalRepository): LocalRepository
}