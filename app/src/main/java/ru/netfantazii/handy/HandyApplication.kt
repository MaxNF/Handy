package ru.netfantazii.handy

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.ShopItem
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.repositories.BillingRepository

class HandyApplication : Application() {
    private val TAG = "HandyApplication"
    val localRepository: LocalRepository
        get() = ServiceLocator.provideLocalRepository(this)
    val remoteRepository: RemoteRepository
        get() = ServiceLocator.provideRemoteRepository()
    val billingRepository: BillingRepository
        get() = ServiceLocator.provideBillingRepository(this)

    val isPremium = ObservableBoolean()

    override fun onCreate() {
        super.onCreate()
        loadSortOrderToMemory()
    }

    private fun loadSortOrderToMemory() {
        currentSortOrder = getSortOrder(this)
    }
}