package ru.netfantazii.handy.repositories

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import io.reactivex.*
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.model.database.CloudFunctions
import ru.netfantazii.handy.model.database.RemoteDbSchema
import java.lang.UnsupportedOperationException

interface RemoteRepository {
    fun sendCatalog(
        catalogContent: Map<String, Any>
    ): Completable

    fun signInToFirebase(credential: AuthCredential): Completable
    fun changeSecret(): Single<String>
    fun addUserUpdateTokenGetSecret(currentDeviceToken: String): Single<String>
    fun getContacts(): Observable<List<Contact>>
    fun addContact(contactData: Map<String, String>): Completable
    fun removeContact(contact: Contact): Completable
    fun updateContact(contact: Contact): Completable
    fun deleteAccount(): Completable
}

class RemoteRepositoryImpl : RemoteRepository {
    override fun sendCatalog(catalogContent: Map<String, Any>): Completable {
        return Completable.create { emitter ->
            val task =
                Firebase.firestore.collection(RemoteDbSchema.COLLECTION_MESSAGES)
                    .add(catalogContent)
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun changeSecret(): Single<String> {
        return Single.create<String> { emitter ->
            val task =
                FirebaseFunctions.getInstance().getHttpsCallable(CloudFunctions.CHANGE_SECRET)
                    .call()
            task.addOnSuccessListener { emitter.onSuccess(it.data as String) }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun addUserUpdateTokenGetSecret(currentDeviceToken: String): Single<String> {
        return Single.create<String> { emitter ->
            val task = FirebaseFunctions.getInstance()
                .getHttpsCallable(CloudFunctions.UPDATE_USER_AND_TOKEN).call()
            task.addOnSuccessListener { emitter.onSuccess(it.data as String) }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun getContacts(): Observable<List<Contact>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addContact(contactData: Map<String, String>): Completable {
        return Completable.create { emitter ->
            val task =
                FirebaseFunctions.getInstance().getHttpsCallable(CloudFunctions.ADD_CONTACT)
                    .call(contactData)
            task.addOnCompleteListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun removeContact(contact: Contact): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContact(contact: Contact): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAccount(): Completable {
        return Completable.create { emitter ->
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                emitter.onError(UnsupportedOperationException("User is not logged in"))
            } else {
                val task = user.delete()
                task.addOnSuccessListener { emitter.onComplete() }
                task.addOnFailureListener { emitter.onError(it) }
            }
        }
    }

    override fun signInToFirebase(credential: AuthCredential): Completable {
        return Completable.create { emitter ->
            val task = FirebaseAuth.getInstance().signInWithCredential(credential)
            task.addOnSuccessListener { emitter.onComplete() }
            task.addOnFailureListener { emitter.onError(it) }
        }
    }
}