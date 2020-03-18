package ru.netfantazii.handy

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.Constants
import ru.netfantazii.handy.data.ShopItem
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.repositories.BillingRepository
import java.util.*

class HandyApplication : Application() {
    private val TAG = "HandyApplication"
    val localRepository: LocalRepository
        get() = ServiceLocator.provideLocalRepository(this)
    val remoteRepository: RemoteRepository
        get() = ServiceLocator.provideRemoteRepository()
    val billingRepository: BillingRepository
        get() = ServiceLocator.provideBillingRepository(this)

    val isPremium = ObservableBoolean()
    var shouldRateDialogBeShown = false

    override fun onCreate() {
        super.onCreate()
        loadSortOrderToMemory()
        saveLaunchCount()
    }

    private fun loadSortOrderToMemory() {
        currentSortOrder = getSortOrder(this)
    }

    private fun saveLaunchCount() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean(Constants.NEVER_SHOW_AGAIN_KEY, false)) {
            return
        }
        var firstLaunchDate = sp.getLong(Constants.FIRST_LAUNCH_DATE_KEY, Date().time)
        var launchCount =
            sp.getInt(Constants.LAUNCH_COUNT_KEY, 0) + 1 // +1 потому, что считаем текущий запуск
        Log.d(TAG, "saveLaunchCount: $firstLaunchDate, $launchCount")

        val currentTime = Date().time
        val isTimePassed = (currentTime - firstLaunchDate) >= Constants.DAYS_BEFORE_RATE_DIALOG
        val isCountPassed = launchCount >= Constants.LAUNCHES_BEFORE_RATE_DIALOG

        shouldRateDialogBeShown = isTimePassed && isCountPassed

        if (shouldRateDialogBeShown) {
            firstLaunchDate = Date().time
            launchCount = 0
        }
        sp.edit()
            .putLong(Constants.FIRST_LAUNCH_DATE_KEY, firstLaunchDate)
            .putInt(Constants.LAUNCH_COUNT_KEY, launchCount)
            .apply()
    }
}