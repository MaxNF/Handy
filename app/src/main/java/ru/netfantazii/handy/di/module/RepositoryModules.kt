package ru.netfantazii.handy.di.module

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.repositories.*

@Module
abstract class LocalRepositoryModule {

    @Binds
    abstract fun bindLocalRepository(localRepo: LocalRepositoryImpl): LocalRepository
}

@Module
abstract class RemoteRepositoryModule {

    @Binds
    abstract fun bindRemoteRepository(remoteRepo: RemoteRepositoryImpl): RemoteRepository
}

@Module
abstract class BillingRepositoryModule {

    @Binds
    abstract fun bindBillingRepository(billingRepoImpl: BillingRepositoryImpl): BillingRepository
}