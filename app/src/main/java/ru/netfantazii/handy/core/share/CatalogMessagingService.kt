package ru.netfantazii.handy.core.share

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

    override fun onMessageReceived(message: RemoteMessage) {
        val remoteRepository = (application as HandyApplication).remoteRepository
        val localRepository = (application as HandyApplication).localRepository


        // для jobScheduler
        val data = message.data
        val date = data[RemoteDbSchema.MESSAGE_DATE]
        val fromName = data[RemoteDbSchema.MESSAGE_FROM_NAME]
        val fromEmail = data[RemoteDbSchema.MESSAGE_FROM_EMAIL]
        val fromImage = data[RemoteDbSchema.MESSAGE_FROM_IMAGE]
        val catalogName = data[RemoteDbSchema.MESSAGE_CATALOG_NAME]
        val catalogComment = data[RemoteDbSchema.MESSAGE_CATALOG_COMMENT]
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "new message token received: $token")
        //todo добавить токен в базу
    }
}