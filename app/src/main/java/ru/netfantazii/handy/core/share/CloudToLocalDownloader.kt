package ru.netfantazii.handy.core.share

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.NOTIFICATION_CHANNEL_ID
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.*
import ru.netfantazii.handy.extensions.getCancelPendingIntentForNotifications
import ru.netfantazii.handy.extensions.getNewCatalogPosition
import ru.netfantazii.handy.extensions.getNotificationSoundUri
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.model.*
import ru.netfantazii.handy.model.database.CatalogNetInfoEntity
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Утилитный класс для вызова из FirebaseMessagingService и JobScheduler.*/
class CloudToLocalDownloader(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val context: Context
) {
    private val TAG = "CatalogMessagingService"
    private val disposables = CompositeDisposable()

    fun downloadCatalogToLocalDb(
        messageId: String,
        failTimeoutSec: Long,
        timeoutAction: () -> Unit,
        successAction: (() -> Unit)?,
        countDownLatch: CountDownLatch?
    ) {
        Log.d(TAG,
            "downloadCatalogToLocalDb: start, thread ${Thread.currentThread().name}, messageId: $messageId")
        disposables.add(remoteRepository.downloadCatalogDataFromMessage(messageId)
            .subscribeOn(Schedulers.io())
            .timeout(failTimeoutSec, TimeUnit.SECONDS)
            .flatMap { catalogData ->
                localRepository.getCatalogsSignleTime().subscribeOn(Schedulers.io())
                    .map { catalogList ->
                        Pair(catalogData, catalogList)
                    }
            }
            .flatMap { (catalogData, catalogList) ->
                Log.d(TAG,
                    "downloadCatalogToLocalDb: flatmap, thread ${Thread.currentThread().name}")

                val catalog = parseCatalog(catalogData)
                val catalogPosition = getNewCatalogPosition(catalogList)
                catalogList.add(catalogPosition, catalog)
                catalogList.reassignPositions()

                val catalogNetInfo = parseNetInfo(catalogData)
                val parsedGroups = parseGroups(catalogData)
                parsedGroups.reassignPositions()

                localRepository.addCatalogWithNetInfoAndProductsAndUpdatePositions(catalog,
                    parsedGroups,
                    catalogNetInfo,
                    catalogList)
                    .subscribeOn(Schedulers.io())
                    .map { catalogId ->
                        catalog.id = catalogId
                        Log.d(TAG,
                            "downloadCatalogToLocalDb: map: thread ${Thread.currentThread().name}")
                        Pair(catalog, catalogNetInfo)
                    }
            }
            .doAfterTerminate {
                countDownLatch?.countDown()
            }
            .subscribe(
                { (first, second) ->
                    Log.d(TAG,
                        "downloadCatalogToLocalDb: success thread ${Thread.currentThread().name}")
                    sendCatalogReceivedNotification(first, second)
                    successAction?.invoke()
                },
                {
                    Log.d(TAG, "downloadCatalogToLocalDb: ")
                    it.printStackTrace()
                    if (it is TimeoutException) {
                        timeoutAction()
                    }
                }))
        countDownLatch?.await()
    }

    private fun parseGroups(catalogData: Map<String, Any>): List<Group> {
        val groupsArray =
            (catalogData[RemoteDbSchema.MESSAGE_CATALOG_CONTENT] as List<Map<String, Any>>)
        val groups = groupsArray.mapIndexed { index, groupContent ->
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
        return groups
    }

    private fun parseCatalog(catalogData: Map<String, Any>): Catalog {
        val catalogName = catalogData[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
        return Catalog(name = catalogName, fromNetwork = true)
    }

    private fun parseNetInfo(catalogData: Map<String, Any>): CatalogNetInfoEntity {
        val receiveTime = Calendar.getInstance()
        val fromName = catalogData[RemoteDbSchema.MESSAGE_FROM_NAME] as String
        val fromEmail = catalogData[RemoteDbSchema.MESSAGE_FROM_EMAIL] as String
        val fromImage = catalogData[RemoteDbSchema.MESSAGE_FROM_IMAGE] as String
        val commentary = catalogData[RemoteDbSchema.MESSAGE_CATALOG_COMMENT] as String
        return CatalogNetInfoEntity(
            receiveTime = receiveTime,
            fromEmail = fromEmail,
            fromName = fromName,
            fromImage = Uri.parse(fromImage),
            commentary = commentary)
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

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(toProductsPendingIntent(catalog))
            .setSound(getNotificationSoundUri())
            .addAction(0,
                context.getString(R.string.notification_cancel_label),
                getCancelPendingIntentForNotifications(context, notificationId))
            .build()
    }

    private fun catalogArguments(
        catalog: Catalog
    ) = Bundle().apply {
        putInt(BUNDLE_DESTINATION_ID_KEY, R.id.products_fragment)
        putLong(BUNDLE_CATALOG_ID_KEY, catalog.id)
        putString(BUNDLE_CATALOG_NAME_KEY, catalog.name)
        putParcelable(BUNDLE_EXPAND_STATE_KEY, catalog.groupExpandStates)
    }

    fun clearResources() {
        disposables.clear()
    }

    private fun toProductsPendingIntent(catalog: Catalog): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)!!.apply {
            putExtras(catalogArguments(catalog))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(context,
            catalog.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
//            return NavDeepLinkBuilder(context)
//                .setGraph(R.navigation.nav_graph)
//                .setDestination(R.id.products_fragment)
//                .setArguments(catalogArguments(catalog))
//                .createPendingIntent()

    }

    private fun truncateCommentary(comment: String): String {
        return if (comment.length > 53) {
            comment.take(50).padEnd(3, '.')
        } else comment
    }
}