package ru.netfantazii.handy.repositories

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.netfantazii.handy.model.Contact

interface RemoteRepository {
    fun getContacts(): Observable<Contact>
    fun addContact(contact: Contact): Disposable
    fun removeContact(contact: Contact): Disposable
}

class RemoteRepositoryImpl() : RemoteRepository {
    override fun getContacts(): Observable<Contact> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addContact(contact: Contact): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeContact(contact: Contact): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}