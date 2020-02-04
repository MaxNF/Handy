package ru.netfantazii.handy.core.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.repositories.RemoteRepository

class ContactsViewModel(private val remoteRepository: RemoteRepository) : ViewModel(),
    DialogClickHandler, ContactsClickHandler, ContactsStorage {


    override fun getContacts(): List<Contact> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onAddContactClick() {

    }

    override fun onDeleteYesClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onEditYesClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onContactDeleteClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onContactEditClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

class ContactsVmFactory(
    private val remoteRepository: RemoteRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(remoteRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}