package ru.netfantazii.handy.core.share

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
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
    private val context: Context
) {
    private val disposables = CompositeDisposable()

    fun downloadCatalogToLocalDb(
        messageId: String,
        failTimeoutSec: Long,
        timeoutAction: () -> Unit
    ) {
        disposables.add(remoteRepository.downloadCatalogDataFromMessage(messageId)
            .timeout(failTimeoutSec, TimeUnit.SECONDS)
            .subscribe({ catalogData ->
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

                addCatalogToLocalDb(catalog, catalogNetInfo)
                sendCatalogReceivedNotification(catalog, catalogNetInfo)
            }, {
                if (it is TimeoutException) {
                    timeoutAction()
                }
            }))
    }

    private fun addCatalogToLocalDb(
        catalog: Catalog,
        catalogNetInfo: CatalogNetInfoEntity
    ): Catalog {
        disposables.add(localRepository.addCatalogWithNetInfo(catalog, catalogNetInfo)
            .subscribe { id ->
                catalog.id = id
            })
        assert(catalog.id != 0L)
        return catalog
    }

    private fun sendCatalogReceivedNotification(
        catalog: Catalog,
        catalogNetInfo: CatalogNetInfoEntity
    ) {
        val title = context.getString(R.string.catalog_received_notification_title)
        val message = context.getString(R.string.catalog_received_notification_message,
            catalog.name,
            catalogNetInfo.fromName,
            truncateCommentary(catalogNetInfo.commentary))
        val notificationId = catalog.id.toInt()
        NotificationManagerCompat.from(context)
            .notify(notificationId,
                createCatalogReceivedNotification(title, message, catalog, notificationId))
    }

    private fun createCatalogReceivedNotification(
        title: String,
        message: String,
        catalog: Catalog,
        notificationId: Int
    ): Notification {
        val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = CANCEL_NOTIFICATION_ACTION
            putExtra(BUNDLE_NOTIFICATION_ID_KEY, notificationId)
        }
        val onCancelClickIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductsPendingIntent(catalog))
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                onCancelClickIntent)
            .build()
    }

    private fun catalogArguments(
        catalog: Catalog
    ) = Bundle().apply {
        putLong(BUNDLE_CATALOG_ID_KEY, catalog.id)
        putString(BUNDLE_CATALOG_NAME_KEY, catalog.name)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, catalog.groupExpandStates)
    }

    fun clearResources() {
        disposables.clear()
    }

    private fun toProductsPendingIntent(catalog: Catalog) = NavDeepLinkBuilder(context)
        .setGraph(R.navigation.nav_graph)
        .setDestination(R.id.products_fragment)
        .setArguments(catalogArguments(catalog))
        .createPendingIntent()

    private fun truncateCommentary(comment: String): String {
        return if (comment.length > 53) {
            comment.take(50).padEnd(3, '.')
        } else comment
    }
}