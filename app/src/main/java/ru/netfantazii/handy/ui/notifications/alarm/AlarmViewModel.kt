package ru.netfantazii.handy.ui.notifications.alarm

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.model.Event
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.utils.registerAlarm
import ru.netfantazii.handy.utils.unregisterAlarm
import java.util.*
import javax.inject.Inject

class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localRepository: LocalRepository,
    @CatalogId private val currentCatalogId: Long,
    @CatalogName private val catalogName: String,
    private val expandStates: RecyclerViewExpandableItemManager.SavedState
) :
    ViewModel() {
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
            time = if (it.isEmpty()) null else
                it[0]
        })
    }

    private fun onNewDataReceive(value: Calendar?) {
        Log.d(TAG, "onNewDataReceive: $value")
        switchStatus.set(value != null)
        _newDataReceived.value = Event(value)
    }

    fun onSwitchClick(datePicker: DatePicker, timePicker: TimePicker) {
        Log.d(TAG, "onCheckedChanged: ")
        val hour: Int
        val minute: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.hour
            minute = timePicker.minute
        } else {
            hour = timePicker.currentHour
            minute = timePicker.currentMinute
        }

        val calendar =
            Calendar.getInstance().apply {
                set(datePicker.year,
                    datePicker.month,
                    datePicker.dayOfMonth,
                    hour,
                    minute,
                    0)
            }
        if (switchStatus.get()!!) applyAlarm(calendar) else cancelAlarm()
    }

    private fun applyAlarm(calendar: Calendar) {
        Log.d(TAG, "registerAlarm: ")
        localRepository.addCatalogAlarmTime(currentCatalogId, calendar)
        registerAlarm(context,
            currentCatalogId,
            catalogName,
            expandStates,
            calendar)
    }

    private fun cancelAlarm() {
        Log.d(TAG, "unregisterAlarm: ")
        localRepository.removeCatalogAlarmTime(currentCatalogId)
        unregisterAlarm(context, currentCatalogId)
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: ")
        disposables.clear()
    }
}

//class AlarmVmFactory(
//    private val application: Application,
//    private val localRepository: LocalRepository,
//    private val catalogId: Long,
//    private val catalogName: String,
//    private val expandStates: RecyclerViewExpandableItemManager.SavedState
//) :
//    ViewModelProvider.AndroidViewModelFactory(application) {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
//            return AlarmViewModel(application,
//                localRepository,
//                catalogId,
//                catalogName,
//                expandStates) as T
//        }
//        throw IllegalArgumentException("Wrong ViewModel class")
//    }
//}