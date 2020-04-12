package ru.netfantazii.handy.core.notifications.alarm.usecases

import android.app.AlarmManager
import android.content.Context
import ru.netfantazii.handy.core.notifications.ALARM_INTENT_ACTION
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.extensions.getPendingIntentForCancel
import javax.inject.Inject

@FragmentScope
class UnregisterAlarmUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    fun unregisterAlarm(catalogId: Long) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent =
            getPendingIntentForCancel(
                context,
                catalogId,
                ALARM_INTENT_ACTION)
        alarmManager.cancel(pendingIntent)
    }
}