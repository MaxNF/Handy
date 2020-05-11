package ru.netfantazii.handy.data.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.data.repositories.LocalRepository
import ru.netfantazii.handy.data.repositories.RemoteRepository
import javax.inject.Inject

const val DOWNLOAD_CATALOG_JOB_ID = 0
const val MESSAGE_ID_BUNDLE_KEY = "message_id"

class CatalogDownloadJobService : JobService() {

    private val TAG = "CatalogMessagingService"

    @Inject
    lateinit var localRepository: LocalRepository
    @Inject
    lateinit var remoteRepository: RemoteRepository

    override fun onCreate() {
        super.onCreate()
        (applicationContext as HandyApplication).appComponent.notificationComponent().create()
            .inject(this)
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStartJob: STARTING JOB")
        val messageId = params.extras.getString(MESSAGE_ID_BUNDLE_KEY)!!
        Log.d(TAG, "onStartJob: $messageId")
        val downloader =
            CloudToLocalDownloader(
                localRepository,
                remoteRepository,
                applicationContext)

        downloader.downloadCatalogToLocalDb(messageId, 5L, {
            jobFinished(params, true)
        }, {
            jobFinished(params, false)
        }, null)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob: planning a job again")
        return true
    }

}