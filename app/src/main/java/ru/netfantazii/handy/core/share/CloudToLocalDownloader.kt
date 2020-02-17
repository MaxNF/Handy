package ru.netfantazii.handy.core.share

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.*
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.model.Catalog
import ru.netfantazii.handy.model.Group
import ru.netfantazii.handy.model.GroupType
import ru.netfantazii.handy.model.Product
import ru.netfantazii.handy.model.database.CatalogNetInfoEntity
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import java.util.*
import java.util.concurrent.CountDownLatch
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
    private val TAG = "nettt"
    private val disposables = CompositeDisposable()

    //    fun downloadCatalogToLocalDb(
//        messageId: String,
//        failTimeoutSec: Long,
//        timeoutAction: () -> Unit
//    ) {
//        val catalogData = remoteRepository.downloadCatalogDataFromMessage(messageId)
//            .timeout(failTimeoutSec, TimeUnit.SECONDS).blockingGet()
//        val catalogName = catalogData[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
//        val catalog = Catalog(name = catalogName)
//        val receiveTime = Calendar.getInstance()
//        val fromName = catalogData[RemoteDbSchema.MESSAGE_FROM_NAME] as String
//        val fromEmail = catalogData[RemoteDbSchema.MESSAGE_FROM_EMAIL] as String
//        val fromImage = catalogData[RemoteDbSchema.MESSAGE_FROM_IMAGE] as String
//        val commentary = catalogData[RemoteDbSchema.MESSAGE_CATALOG_COMMENT] as String
//        val catalogNetInfo = CatalogNetInfoEntity(receiveTime = receiveTime,
//            fromEmail = fromEmail,
//            fromName = fromName,
//            fromImage = Uri.parse(fromImage),
//            commentary = commentary)
//
//        disposables.add(localRepository.addCatalogWithNetInfo(catalog, catalogNetInfo)
//            .subscribe { _ ->
//                sendCatalogReceivedNotification(catalog, catalogNetInfo)
//            })
//    }
    fun downloadCatalogToLocalDb(
        messageId: String,
        failTimeoutSec: Long,
        timeoutAction: () -> Unit
    ) {
        Log.d(TAG, "downloadCatalogToLocalDb: start, thread ${Thread.currentThread().name}")
        val latch = CountDownLatch(1)
        disposables.add(remoteRepository.downloadCatalogDataFromMessage(messageId)
            .timeout(failTimeoutSec, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .flatMap { catalogData ->
                Log.d(TAG,
                    "downloadCatalogToLocalDb: flatmap, thread ${Thread.currentThread().name}")
                val catalogName = catalogData[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
                val catalog = Catalog(name = catalogName, fromNetwork = true)
                val receiveTime = Calendar.getInstance()
                val fromName = catalogData[RemoteDbSchema.MESSAGE_FROM_NAME] as String
                val fromEmail = catalogData[RemoteDbSchema.MESSAGE_FROM_EMAIL] as String
                val fromImage = catalogData[RemoteDbSchema.MESSAGE_FROM_IMAGE] as String
                val commentary = catalogData[RemoteDbSchema.MESSAGE_CATALOG_COMMENT] as String
                val catalogNetInfo = CatalogNetInfoEntity(receiveTime = receiveTime,
                    fromEmail = fromEmail,
                    fromName = fromName,
                    fromImage = Uri.parse(fromImage),
                    commentary = commentary)
                // todo сделать различие между дефолтной группой и остальными
                val groupsArray =
                    (catalogData[RemoteDbSchema.MESSAGE_CATALOG_CONTENT] as List<Map<String, Any>>)
                val parsedGroups = groupsArray.mapIndexed { index, groupContent ->
                    val groupType = if (index == 0) GroupType.ALWAYS_ON_TOP else GroupType.STANDARD

                    val group =
                        Group(name = groupContent[RemoteDbSchema.MESSAGE_GROUP_NAME] as String,
                            catalogId = 0L, groupType = groupType)

                    group.productList =
                        (groupContent[RemoteDbSchema.MESSAGE_GROUP_PRODUCTS] as List<String>).map { productName ->
                            Product(name = productName, catalogId = 0, groupId = 0)
                        }.toMutableList()

                    group
                }
                parsedGroups.reassignPositions()

                localRepository.addCatalogWithNetInfoAndProducts(catalog,
                    parsedGroups,
                    catalogNetInfo)
                    .subscribeOn(Schedulers.io())
                    .map {
                        Log.d(TAG,
                            "downloadCatalogToLocalDb: map: thread ${Thread.currentThread().name}")
                        Pair(catalog, catalogNetInfo)
                    }
            }
            .subscribe(
                { (first, second) ->
                    Log.d(TAG,
                        "downloadCatalogToLocalDb: success thread ${Thread.currentThread().name}")
                    sendCatalogReceivedNotification(first, second)
                    latch.countDown()
                },
                {
                    Log.d(TAG, "downloadCatalogToLocalDb: ")
                    it.printStackTrace()
                    if (it is TimeoutException) {
                        timeoutAction()
                    }
                    latch.countDown()
                }))
        latch.await()
    }

//    private fun addCatalogToLocalDb(
//        catalog: Catalog,
//        catalogNetInfo: CatalogNetInfoEntity
//    ): Catalog {
//        disposables.add(localRepository.addCatalogWithNetInfoAndProducts(catalog, catalogNetInfo)
//            .subscribe { id ->
//                catalog.id = id
//            })
//        assert(catalog.id != 0L)
//        return catalog
//    }

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