package ru.netfantazii.handy.data.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import io.reactivex.*
import io.reactivex.Observable
import ru.netfantazii.handy.data.model.Contact
import ru.netfantazii.handy.data.remotedb.CloudFunctions
import ru.netfantazii.handy.data.remotedb.RemoteDbSchema
import java.lang.UnsupportedOperationException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteRepository {
    fun sendCatalog(
        catalogContent: Map<String, Any>
    ): Completable

    fun signInToFirebase(credential: AuthCredential): Completable
    fun changeSecret(): Single<String>
    fun addUserUpdateTokenGetSecret(): Single<Pair<String, String>>
    fun getContacts(): Observable<List<Contact>>
    fun addContact(contactData: Map<String, String>): Completable
    fun removeContact(contactData: Map<String, String>): Completable
    fun updateContact(contactData: Map<String, String>): Completable
    fun deleteAccount(): Completable
    fun downloadCatalogDataFromMessage(messageId: String): Single<Map<String, Any>>
    fun addToken(token: String, uid: String): Completable
    fun removeToken(token: String, uid: String): Completable
    fun removeTokenOnLogout(): Completable
    fun reauthenticateInFirebase(credential: AuthCredential): Completable
}

@Singleton
class RemoteRepositoryImpl @Inject constructor() : RemoteRepository {
    private val TAG = "RemoteRepositoryImpl"
    val TIMEOUT_TIME_SEC = 10L

    private val firestoreHttpsEuWest1 =
        FirebaseFunctions.getInstance(CloudFunctions.REGION_EU_WEST1)

    override fun downloadCatalogDataFromMessage(messageId: String): Single<Map<String, Any>> {
        return Single.create<Map<String, Any>> { emitter ->
            val task =
                Firebase.firestore.collection(RemoteDbSchema.COLLECTION_MESSAGES)
                    .document(messageId)
                    .get()
            task.addOnSuccessListener {
                if (it.data == null) {
                    emitter.onError(Exception("Received catalog is null!"))
                } else {
                    emitter.onSuccess(it.data!!)
                }
            }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun sendCatalog(catalogContent: Map<String, Any>) = Completable.create { emitter ->
        val task =
            firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.SEND_CATALOG)
                .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS).call(catalogContent)
        task.addOnSuccessListener { emitter.onComplete() }
        task.addOnFailureListener { emitter.onError(it) }
    }

    override fun changeSecret(): Single<String> {
        return Single.create<String> { emitter ->
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.CHANGE_SECRET)
                    .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS)
                    .call()
            task.addOnSuccessListener { emitter.onSuccess(it.data as String) }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun addUserUpdateTokenGetSecret(): Single<Pair<String, String>> {
        return Single.create<Pair<String, String>> { emitter ->
            val task = firestoreHttpsEuWest1
                .getHttpsCallable(CloudFunctions.UPDATE_USER_AND_TOKEN)
                .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS)
                .call()

            task.addOnSuccessListener {
                val map = it.data as Map<String, String>
                emitter.onSuccess(Pair(map[RemoteDbSchema.USER_SHORT_ID] as String,
                    map[RemoteDbSchema.ANOTHER_SECRET] as String))
            }
            task.addOnFailureListener {
                emitter.onError(it)
            }
        }
    }

    override fun getContacts(): Observable<List<Contact>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw UnsupportedOperationException("User is null!")
        return Observable.create { emitter ->
            val databaseListener =
                Firebase.firestore.collection(RemoteDbSchema.COLLECTION_FRIENDS).document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            emitter.onError(error)
                        } else {
                            val data = snapshot?.data
                            val result = data?.map { entry ->
                                val contactData = entry.value as Map<String, Any>
                                val secret = entry.key
                                val name = contactData[RemoteDbSchema.FRIEND_NAME] as String
                                val firebaseTimestamp =
                                    contactData[RemoteDbSchema.FRIEND_TIME] as Timestamp
                                val isValid = contactData[RemoteDbSchema.FRIEND_VALID] as Boolean
                                val date = Calendar.getInstance()
                                    .apply { time = firebaseTimestamp.toDate() }
                                Contact(name,
                                    secret,
                                    date,
                                    isValid)
                            } ?: listOf()
                            emitter.onNext(result)
                        }
                    }
            emitter.setCancellable { databaseListener.remove() }
        }
    }

    override fun addContact(contactData: Map<String, String>): Completable {
        return Completable.create { emitter ->
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.ADD_CONTACT)
                    .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS)
                    .call(contactData)
            task.addOnCompleteListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun removeContact(contactData: Map<String, String>): Completable {
        return Completable.create { emitter ->
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.DELETE_CONTACT)
                    .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS)
                    .call(contactData)
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun updateContact(contactData: Map<String, String>): Completable {
        return Completable.create { emitter ->
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.UPDATE_CONTACT)
                    .withTimeout(TIMEOUT_TIME_SEC, TimeUnit.SECONDS)
                    .call(contactData)
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun deleteAccount() = Completable.create { emitter ->
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            emitter.onError(UnsupportedOperationException("User is not logged in"))
        } else {
            val task = user.delete()
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun signInToFirebase(credential: AuthCredential) =
        Completable.create { emitter ->
            val task = FirebaseAuth.getInstance().signInWithCredential(credential)
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }

    override fun addToken(token: String, uid: String) = Completable.create { emitter ->
        val task = Firebase.firestore.collection(RemoteDbSchema.COLLECTION_USERS).document(uid)
            .update(RemoteDbSchema.USER_DEVICE_TOKENS, FieldValue.arrayUnion(token))
        task.addOnSuccessListener { emitter.onComplete() }
        task.addOnFailureListener { emitter.onError(it) }
    }

    override fun removeToken(token: String, uid: String) =
        Completable.create { emitter ->
            val task = Firebase.firestore.collection(RemoteDbSchema.COLLECTION_USERS).document(uid)
                .update(RemoteDbSchema.USER_DEVICE_TOKENS, FieldValue.arrayRemove(token))
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }

    override fun removeTokenOnLogout() = Completable.create { emitter ->
        val task =
            firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.DELETE_TOKEN_ON_LOGOUT).call()
        task.addOnSuccessListener { emitter.onComplete() }
        task.addOnFailureListener { emitter.onError(it) }
    }

    override fun reauthenticateInFirebase(credential: AuthCredential) =
        Completable.create { emitter ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                emitter.onError(IllegalArgumentException("Current user is null!"))
            } else {
                val task = currentUser.reauthenticate(credential)
                task.addOnSuccessListener { emitter.onComplete() }
                task.addOnFailureListener { emitter.onError(it) }
            }
        }

}