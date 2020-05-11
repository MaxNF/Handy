package ru.netfantazii.handy.data.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.PersistableBundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.repositories.RemoteRepository
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class CatalogMessagingService : FirebaseMessagingService() {
    private val preferenceTokenKey = "prev_token"
    private val TAG = "CatalogMessagingService"
    private lateinit var downloader: CloudToLocalDownloader

    @Inject
    lateinit var remoteRepository: RemoteRepository
    @Inject
    lateinit var localRepository: LocalRepository

    override fun onCreate() {
        super.onCreate()
        (applicationContext as HandyApplication).appComponent.notificationComponent().create()
            .inject(this)
        downloader = CloudToLocalDownloader(
            localRepository,
            remoteRepository,
            applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val messageId = message.data["message_id"] as String
        val countDownLatch = CountDownLatch(1)
        downloader.downloadCatalogToLocalDb(messageId, 5L, {
            Log.d(TAG, "PLANNING DOWNLOAD")
            planDownload(messageId)
        }, null, countDownLatch)
        countDownLatch.await()
        Log.d(TAG, "onMessageReceived: thread ${Thread.currentThread().name}")
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
        val uid = FirebaseAuth.getInstance().uid ?: return
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val previousToken = sp.getString(preferenceTokenKey, null)
        putNewTokenToPreferences(sp, token)
        if (previousToken != null) {
            remoteRepository.removeToken(previousToken, uid).subscribeOn(Schedulers.io())
                .subscribe()
        }
        remoteRepository.addToken(token, uid).subscribeOn(Schedulers.io()).subscribe()
    }

    private fun putNewTokenToPreferences(sp: SharedPreferences, token: String) {
        sp.edit(true) {
            putString(preferenceTokenKey, token)
        }
    }

}