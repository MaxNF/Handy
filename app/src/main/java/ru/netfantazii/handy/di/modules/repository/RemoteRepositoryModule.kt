package ru.netfantazii.handy.di.modules.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.data.repositories.RemoteRepository
import ru.netfantazii.handy.data.repositories.RemoteRepositoryImpl

@Module
abstract class RemoteRepositoryModule {

    @Binds
    abstract fun bindRemoteRepository(remoteRepo: RemoteRepositoryImpl): RemoteRepository
}