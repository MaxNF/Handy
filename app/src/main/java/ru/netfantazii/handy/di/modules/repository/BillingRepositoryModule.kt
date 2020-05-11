package ru.netfantazii.handy.di.modules.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.data.repositories.BillingRepository
import ru.netfantazii.handy.data.repositories.BillingRepositoryImpl

@Module
abstract class BillingRepositoryModule {

    @Binds
    abstract fun bindBillingRepository(billingRepo: BillingRepositoryImpl): BillingRepository
}