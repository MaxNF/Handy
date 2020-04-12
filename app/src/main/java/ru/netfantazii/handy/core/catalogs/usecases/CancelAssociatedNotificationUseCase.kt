package ru.netfantazii.handy.core.catalogs.usecases

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope
import javax.inject.Inject

@FragmentScope
class CancelAssociatedNotificationUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Отменяет все уведомления (по времени и связанные с геозонами), которые относятся к этому
     * списку покупок.
     * */
    fun cancelAssociatedNotifications(catalogId: Long) {
        val notificationId = catalogId.toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}