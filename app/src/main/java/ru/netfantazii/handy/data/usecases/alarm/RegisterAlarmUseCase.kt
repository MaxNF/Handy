package ru.netfantazii.handy.data.usecases.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.data.receiver.ALARM_INTENT_ACTION
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.utils.getPendingIntentForNotification
import java.util.*
import javax.inject.Inject

@FragmentScope
class RegisterAlarmUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    fun registerAlarm(
        catalogId: Long,
        catalogName: String,
        expandStates: RecyclerViewExpandableItemManager.SavedState,
        triggerTime: Calendar
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            getPendingIntentForNotification(
                context,
                catalogId,
                catalogName,
                expandStates,
                ALARM_INTENT_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
        }
    }
}