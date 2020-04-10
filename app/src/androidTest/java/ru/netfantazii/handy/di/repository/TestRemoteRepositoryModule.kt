package ru.netfantazii.handy.di.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.FakeRemoteRepository
import ru.netfantazii.handy.repositories.RemoteRepository

@Module
abstract class TestRemoteRepositoryModule {

    @Binds
    abstract fun bindRemoteRepository(remoteRepo: FakeRemoteRepository): RemoteRepository
}