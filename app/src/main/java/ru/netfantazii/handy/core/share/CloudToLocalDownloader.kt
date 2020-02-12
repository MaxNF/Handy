package ru.netfantazii.handy.core.share

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.core.notifications.*
import ru.netfantazii.handy.model.Catalog
import ru.netfantazii.handy.model.database.CatalogNetInfoEntity
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Утилитный класс для вызова из FirebaseMessagingService и JobScheduler. Производит работу в
 * вызванном потоке! (не передает обращения к облачной и локальной бд в отдельный поток)*/
class CloudToLocalDownloader(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val disposables: CompositeDisposable = CompositeDisposable(),
    private val context: Context
) {
    fun downloadCatalogToLocalDb(
        messageId: String,
        failTimeoutSec: Long,
        timeoutAction: () -> Unit
    ) {
        disposables.add(remoteRepository.downloadCatalogDataFromMessage(messageId)
            .timeout(failTimeoutSec, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.trampoline())
            .subscribe({ catalogData ->
                parseDataAndAddToLocalDb(catalogData)
            }, {
                if (it is TimeoutException) {
                    timeoutAction()
                }
            }))
    }

    private fun parseDataAndAddToLocalDb(catalogData: Map<String, Any>) {
        val catalogName = catalogData[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
        val catalog = Catalog(name = catalogName)

        val dateMs = catalogData[RemoteDbSchema.MESSAGE_DATE] as Date
        val receiveTime = Calendar.getInstance().apply { time = dateMs }
        val fromName = catalogData[RemoteDbSchema.MESSAGE_FROM_NAME] as String
        val fromEmail = catalogData[RemoteDbSchema.MESSAGE_FROM_EMAIL] as String
        val fromImage = catalogData[RemoteDbSchema.MESSAGE_FROM_IMAGE] as String
        val commentary = catalogData[RemoteDbSchema.MESSAGE_CATALOG_COMMENT] as String
        val catalogNetInfo = CatalogNetInfoEntity(receiveTime = receiveTime,
            fromEmail = fromEmail,
            fromName = fromName,
            fromImage = Uri.parse(fromImage),
            commentary = commentary)

        localRepository.addCatalogWithNetInfo(catalog, catalogNetInfo)
            .subscribeOn(Schedulers.trampoline()).subscribe()
    }

    private fun createGeofenceNotification(
        title: String,
        message: String,
        geofenceIds: LongArray
    ): Notification {
        val toProductIntent = Intent(context, NotificationService::class.java).apply {
            action = GEOFENCE_TO_PRODUCTS_INTENT_ACTION
            putExtra(BUNDLE_KEY, geofenceArguments(geofenceIds))
        }

        val toProductPendingIntent = PendingIntent.getService(context,
            notificationId,
            toProductIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductPendingIntent)
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .build()
    }

    private fun geofenceArguments(geofenceIds: LongArray) = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalogId)
        putString(BUNDLE_CATALOG_NAME_KEY, catalogName)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, groupExpandState)
        putBoolean(BUNDLE_FROM_GEOFENCE_NOTIFICATION_KEY, true)
        putLongArray(BUNDLE_GEOFENCE_IDS_KEY, geofenceIds)
    }

    fun clearResources() {
        disposables.clear()
    }
}