package ru.netfantazii.handy.di.repository

import dagger.Binds
import dagger.Module
import ru.netfantazii.handy.FakeBillingRepository
import ru.netfantazii.handy.repositories.BillingRepository

@Module
abstract class TestBillingRepositoryModule {

    @Binds
    abstract fun bindBillingRepository(billingRepo: FakeBillingRepository): BillingRepository
}