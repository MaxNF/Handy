package ru.netfantazii.handy

import com.google.firebase.auth.AuthCredential
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.data.model.Contact
import ru.netfantazii.handy.data.repositories.RemoteRepository
import javax.inject.Inject

class FakeRemoteRepository @Inject constructor() : RemoteRepository {
    override fun sendCatalog(catalogContent: Map<String, Any>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signInToFirebase(credential: AuthCredential): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeSecret(): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addUserUpdateTokenGetSecret(): Single<Pair<String, String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContacts(): Observable<List<Contact>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addContact(contactData: Map<String, String>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeContact(contactData: Map<String, String>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContact(contactData: Map<String, String>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAccount(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadCatalogDataFromMessage(messageId: String): Single<Map<String, Any>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addToken(token: String, uid: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeToken(token: String, uid: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeTokenOnLogout(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reauthenticateInFirebase(credential: AuthCredential): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}