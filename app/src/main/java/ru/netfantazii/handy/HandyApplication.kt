package ru.netfantazii.handy

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.Constants
import ru.netfantazii.handy.di.AppComponent
import ru.netfantazii.handy.di.DaggerAppComponent
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.repositories.BillingRepository
import java.util.*
import javax.inject.Inject

open class HandyApplication : Application() {
    private val TAG = "HandyApplication"
    val appComponent: AppComponent by lazy {
        initializeComponent()
    }

    val isPremium = ObservableBoolean()
    var shouldRateDialogBeShown = false

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
        loadSortOrderToMemory()
        saveLaunchCount()
    }

    open fun initializeComponent() = DaggerAppComponent.factory().create(applicationContext, packageName)

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