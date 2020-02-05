package ru.netfantazii.handy.core.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.repositories.RemoteRepository

class ContactsViewModel(private val remoteRepository: RemoteRepository) : ViewModel(),
    DialogClickHandler, ContactsClickHandler, ContactsStorage {

    private val _contactSwipePerformed = MutableLiveData<Event<Contact>>()
    val contactSwipePerformed: LiveData<Event<Contact>> = _contactSwipePerformed

    private val _contactEditClicked = MutableLiveData<Event<Contact>>()
    val contactEditClicked: LiveData<Event<Contact>> = _contactEditClicked

    private val _addContactClicked = MutableLiveData<Event<Unit>>()
    val addContactClicked: LiveData<Event<Unit>> = _addContactClicked

    override fun getContacts(): List<Contact> {
        // тут подписываемся на медиатор или главную лайв дату и получаем обновления списка друзей
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

    fun undoRemoval() {

    }

    override fun onContactSwipePerform(contact: Contact) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onContactEditClick(contact: Contact) {
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