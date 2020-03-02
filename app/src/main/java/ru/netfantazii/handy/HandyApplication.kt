package ru.netfantazii.handy

import android.app.Application
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.repositories.BillingRepository

class HandyApplication : Application() {
    val localRepository: LocalRepository
        get() = ServiceLocator.provideLocalRepository(this)
    val remoteRepository: RemoteRepository
        get() = ServiceLocator.provideRemoteRepository()
    val billingRepository: BillingRepository
    get() = ServiceLocator.provideBillingRepository(this)

    override fun onCreate() {
        super.onCreate()
        loadSortOrderToMemory()
    }

    private fun loadSortOrderToMemory() {
        currentSortOrder = getSortOrder(this)
    }
}