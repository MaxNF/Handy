package ru.netfantazii.handy.core.share

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.model.database.RemoteDbSchema

class CatalogMessagingService : FirebaseMessagingService() {
    private val TAG = "CatalogMessagingService"
    private lateinit var downloader: CloudToLocalDownloader

    override fun onMessageReceived(message: RemoteMessage) {

        val remoteRepository = (application as HandyApplication).remoteRepository
        val localRepository = (application as HandyApplication).localRepository
        val messageId = message.data["message_id"] as String
        downloader = CloudToLocalDownloader(localRepository, remoteRepository)
        downloader.downloadCatalogToLocalDb(messageId, 5L) {
            planDownload(messageId)
        }
    }

    private fun planDownload(messageId: String) {

        val jobInfo = JobInfo.Builder(DOWNLOAD_CATALOG_JOB_ID,
            ComponentName(this, CatalogDownloadJobService::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setExtras(PersistableBundle().apply { putString(MESSAGE_ID_BUNDLE_KEY, messageId) })
            .build()
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.clearResources()
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "new message token received: $token")
        //todo добавить токен в базу
    }
}