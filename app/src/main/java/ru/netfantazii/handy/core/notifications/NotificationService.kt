package ru.netfantazii.handy.core.notifications

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.R
import ru.netfantazii.handy.extensions.*
import java.lang.UnsupportedOperationException

const val ALARM_TO_ALARM_INTENT_ACTION = "alarm_to_alarm"
const val ALARM_TO_PRODUCTS_INTENT_ACTION = "alarm_to_products"
const val GEOFENCE_TO_PRODUCTS_INTENT_ACTION = "geofence_to_products"
const val BUNDLE_DESTINATION_ID_KEY = "destination_id"

class NotificationService : IntentService("notification_service") {
    private lateinit var localRepository: LocalRepository
    private lateinit var arguments: Bundle

    override fun onCreate() {
        super.onCreate()
        localRepository = (application as HandyApplication).localRepository
    }

    /**
     * В службе обрабатываем три сценария:
     * 1) Клик по кнопке изменить время будильника у уведомления полученному от AlarmManager.
     * Открываем фрагмент настройки будильника для соответствующего каталога. Удаление текущей
     * записи будильника из БД происходит раньше, сразу на этапе получения уведомления в Броадкаст ресивере.
     *
     * 2) Клик по самому уведомлению полученному от AlarmManager. Убираем уведомление вручную и открываем
     * фрагмент со списком покупок. Удаление текущей записи происходит также как в п.1
     *
     * 3) Клик по самому уведомлению полученному от сработанной геозоны. Удаляем сработанную геозону
     * из GeofencingClient и из базы данных. Также, если есть установленный будильник для этого каталога,
     * удаляем его из БД и снимаем регистрацию. Потом открываем список продуктов соответствующего каталога.*/

    override fun onHandleIntent(intent: Intent?) {
        arguments = intent!!.extras!!.getBundle(BUNDLE_KEY)!!
        when (intent.action) {
            ALARM_TO_ALARM_INTENT_ACTION -> {
                cancelNotification()
                startAlarmToAlarmFragment()
            }
            ALARM_TO_PRODUCTS_INTENT_ACTION -> {
                startAlarmToProductFragment()
            }
            GEOFENCE_TO_PRODUCTS_INTENT_ACTION -> {
                removeAlarmFromDbAndSystem()
                removeGeofenceFromDbAndSystem()
                startGeofenceToProductFragment()
            }
            else -> throw UnsupportedOperationException("Unknown intent action")
        }
    }

    private fun cancelNotification() {
        NotificationManagerCompat.from(applicationContext).cancel(notificationId())
    }

    private fun removeAlarmFromDbAndSystem() {
        localRepository.removeCatalogAlarmTime(catalogId())
        unregisterAlarm(applicationContext, catalogId())
    }

    private fun removeGeofenceFromDbAndSystem() {
        geofenceIds().forEach {
            localRepository.removeGeofenceById(it)
            unregisterGeofence(applicationContext, it, null)
        }
    }

    private fun startAlarmToAlarmFragment() {
        closeTopSystemWindow()
        startActivity(intentForStartingActivity(alarmArguments(R.id.notifications_fragment)))
    }

    private fun startAlarmToProductFragment() {
        startActivity(intentForStartingActivity(alarmArguments(R.id.products_fragment)))
    }

    private fun startGeofenceToProductFragment() {
        startActivity(intentForStartingActivity(geofenceArguments(R.id.products_fragment)))
    }

    private fun closeTopSystemWindow() {
        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        applicationContext.sendBroadcast(closeIntent)
    }

//    private fun alarmToAlarmPendingIntent() = NavDeepLinkBuilder(applicationContext)
//        .setGraph(R.navigation.nav_graph)
//        .setDestination(R.id.notifications_fragment)
//        .setArguments(alarmArguments())
//        .createPendingIntent()


//    private fun alarmToProductsPendingIntent() = NavDeepLinkBuilder(applicationContext)
//        .setGraph(R.navigation.nav_graph)
//        .setDestination(R.id.products_fragment)
//        .setArguments(alarmArguments())
//        .createPendingIntent()

    private fun intentForStartingActivity(arguments: Bundle): Intent {
        val context = applicationContext
        return Intent(context, MainActivity::class.java)!!.apply {
            putExtras(arguments)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

//    private fun geofenceToProductsPendingIntent() = NavDeepLinkBuilder(applicationContext)
//        .setGraph(R.navigation.nav_graph)
//        .setDestination(R.id.products_fragment)
//        .setArguments(geofenceArguments())
//        .createPendingIntent()

//    private fun alarmArguments(): Bundle = Bundle().apply {
//        putLong(BUNDLE_CATALOG_ID_KEY, catalogId())
//        putString(BUNDLE_CATALOG_NAME_KEY, catalogName())
//        putParcelable(BUNDLE_EXPAND_STATE_KEY, expandState())
//    }

    private fun alarmArguments(destination: Int): Bundle = Bundle().apply {
        putInt(BUNDLE_DESTINATION_ID_KEY, destination)
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId())
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName())
        putParcelable(BUNDLE_EXPAND_STATE_KEY, expandState())
    }

    private fun geofenceArguments(destination: Int) = Bundle().apply {
        putInt(BUNDLE_DESTINATION_ID_KEY, destination)
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId())
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName())
        putParcelable(BUNDLE_EXPAND_STATE_KEY, expandState())
        putLongArray(BUNDLE_GEOFENCE_IDS_KEY, geofenceIds())
    }

    private fun catalogId() = arguments.getLong(BUNDLE_CATALOG_ID_KEY)

    private fun catalogName() = arguments.getString(BUNDLE_CATALOG_NAME_KEY)!!

    private fun expandState(): RecyclerViewExpandableItemManager.SavedState =
        arguments.getParcelable(
            BUNDLE_EXPAND_STATE_KEY)!!

    private fun geofenceIds(): LongArray = arguments.getLongArray(BUNDLE_GEOFENCE_IDS_KEY)!!

    private fun notificationId() = arguments.getInt(BUNDLE_NOTIFICATION_ID_KEY)
}