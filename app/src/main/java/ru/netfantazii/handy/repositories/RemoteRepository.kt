package ru.netfantazii.handy.repositories

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.netfantazii.handy.model.Catalog
import ru.netfantazii.handy.model.Contact

interface RemoteRepository {
    fun sendCatalog(
        secret: String,
        catalogName: String,
        catalogComment: String,
        catalogContent: Map<String, Map<String, Any>>
    ): Completable

    fun changeSecret(): Completable
    fun addAndUpdateUser(currentDeviceToken: String): Completable
    fun getContacts(): Observable<Contact>
    fun addContact(contact: Contact): Completable
    fun removeContact(contact: Contact): Completable
}

class RemoteRepositoryImpl() : RemoteRepository {
    override fun sendCatalog(
        secret: String,
        catalogName: String,
        catalogComment: String,
        catalogContent: Map<String, Map<String, Any>>
    ): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeSecret(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAndUpdateUser(currentDeviceToken: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContacts(): Observable<Contact> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addContact(contact: Contact): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeContact(contact: Contact): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}