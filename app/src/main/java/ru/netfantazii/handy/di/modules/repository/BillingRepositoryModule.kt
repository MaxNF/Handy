package ru.netfantazii.handy.di.modules.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.repositories.BillingRepository
import ru.netfantazii.handy.repositories.BillingRepositoryImpl

@Module
abstract class BillingRepositoryModule {

    @Binds
    abstract fun bindBillingRepository(billingRepoImpl: BillingRepositoryImpl): BillingRepository
}