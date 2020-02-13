package ru.netfantazii.handy.core.share

import android.app.job.JobParameters
import android.app.job.JobService
import ru.netfantazii.handy.HandyApplication

const val DOWNLOAD_CATALOG_JOB_ID = 0
const val MESSAGE_ID_BUNDLE_KEY = "message_id"

class CatalogDownloadJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        val remoteRepository = (application as HandyApplication).remoteRepository
        val localRepository = (application as HandyApplication).localRepository
        val messageId = params.extras.getString(MESSAGE_ID_BUNDLE_KEY)!!
        val downloader =
            CloudToLocalDownloader(localRepository, remoteRepository, applicationContext)
        downloader.downloadCatalogToLocalDb(messageId, 5L) {
            jobFinished(params, true)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

}