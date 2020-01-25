package ru.netfantazii.handy.core.notifications.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.yandex.mapkit.Version
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.core.notifications.ALARM_INTENT_ACTION
import ru.netfantazii.handy.core.notifications.GeofenceHandler
import ru.netfantazii.handy.core.notifications.NotificationBroadcastReceiver
import ru.netfantazii.handy.core.notifications.getPendingIntentForNotification
import java.util.*
import kotlin.math.log

class AlarmViewModel(
    application: Application,
    private val localRepository: LocalRepository,
    private val currentCatalogId: Long,
    private val catalogName: String,
    private val expandStates: RecyclerViewExpandableItemManager.SavedState
) :
    AndroidViewModel(application) {
    private val TAG = "AlarmViewModel"
    private val disposables: CompositeDisposable = CompositeDisposable()
    private var time: Calendar? = null
        set(value) {
            Log.d(TAG, "setting calendar: ")
            field = value
            onNewDataReceive(value)
        }
    val switchStatus = ObservableField<Boolean>()

    private val _newDataReceived = MutableLiveData<Event<Calendar?>>()
    val newDataReceived: LiveData<Event<Calendar?>> = _newDataReceived

    init {
        Log.d(TAG, "init: catalogId: $currentCatalogId, catalogName: $catalogName")
        switchStatus.set(false)
        subscribeToAlarmChanges()
    }

    private fun subscribeToAlarmChanges() {
        Log.d(TAG, "subscribeToAlarmChanges: 1")
        disposables.add(localRepository.getCatalogAlarmTime(currentCatalogId).observeOn(
            AndroidSchedulers.mainThread()).subscribe {
            Log.d(TAG, "subscribeToAlarmChanges: 2")
            time = it
        })
    }

    private fun onNewDataReceive(value: Calendar?) {
        Log.d(TAG, "onNewDataReceive: $value")
        switchStatus.set(value != null)
        _newDataReceived.value = Event(value)
    }

    fun onSwitchClick(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int,
        minute: Int
    ) {
        Log.d(TAG, "onCheckedChanged: ")
        val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth, hour, minute, 0) }
        if (switchStatus.get()!!) registerAlarm(calendar) else unregisterAlarm()
    }

    private fun registerAlarm(calendar: Calendar) {
        Log.d(TAG, "registerAlarm: ")
        localRepository.addCatalogAlarmTime(currentCatalogId, calendar)
        val alarmManager =
            (getApplication() as Context).getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntentForNotification(getApplication(),
            currentCatalogId,
            catalogName,
            expandStates,
            ALARM_INTENT_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun unregisterAlarm() {
        Log.d(TAG, "unregisterAlarm: ")
        localRepository.removeCatalogAlarmTime(currentCatalogId)
        val alarmManager =
            (getApplication() as Context).getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntentForNotification(getApplication(),
            currentCatalogId,
            catalogName,
            expandStates,
            ALARM_INTENT_ACTION)
        alarmManager.cancel(pendingIntent)
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: ")
        disposables.clear()
    }
}

class AlarmVmFactory(
    private val application: Application,
    private val localRepository: LocalRepository,
    private val catalogId: Long,
    private val catalogName: String,
    private val expandStates: RecyclerViewExpandableItemManager.SavedState
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(application,
                localRepository,
                catalogId,
                catalogName,
                expandStates) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}