package ru.netfantazii.handy

import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.core.contacts.ContactsClickHandler
import ru.netfantazii.handy.core.contacts.ContactsStorage
import ru.netfantazii.handy.core.contacts.DialogClickHandler
import ru.netfantazii.handy.extensions.copyTextToClipboard
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.model.ContactDialogAction
import ru.netfantazii.handy.model.User
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.RemoteRepository
import java.lang.UnsupportedOperationException

class NetworkViewModel(private val remoteRepository: RemoteRepository) : ViewModel(),
    ContactsStorage, DialogClickHandler, ContactsClickHandler {
    private val TAG = "NetworkViewModel"

    private val disposables = CompositeDisposable()
    private lateinit var contactsUpdateDisposable: Disposable

    private var contacts: List<Contact> = listOf()
        set(value) {
            Log.d(TAG, ": ")
            field = value
            onNewDataReceive()
        }

    val isSending = ObservableField<Boolean>()
    val sendingCatalogName = ObservableField<String>()

    private val _contactSwipePerformed = MutableLiveData<Event<Contact>>()
    val contactSwipePerformed: LiveData<Event<Contact>> = _contactSwipePerformed

    private val _contactEditClicked = MutableLiveData<Event<Contact>>()
    val contactEditClicked: LiveData<Event<Contact>> = _contactEditClicked

    private val _addContactClicked = MutableLiveData<Event<Unit>>()
    val addContactClicked: LiveData<Event<Unit>> = _addContactClicked

    private val _contactsUpdated = MutableLiveData<Event<Unit>>()
    val contactsUpdated: LiveData<Event<Unit>> = _contactsUpdated

    private val _signInClicked = MutableLiveData<Event<Unit>>()
    val signInClicked: LiveData<Event<Unit>> = _signInClicked

    private val _signOutClicked = MutableLiveData<Event<Unit>>()
    val signOutClicked: LiveData<Event<Unit>> = _signOutClicked

    private val _signInComplete = MutableLiveData<Event<Unit>>()
    val signInComplete: LiveData<Event<Unit>> = _signInComplete

    private val _firebaseSignInError = MutableLiveData<Event<Unit>>()
    val firebaseSignInError: LiveData<Event<Unit>> = _firebaseSignInError

    private val _retrievingContactsError = MutableLiveData<Event<Unit>>()
    val retrievingContactsError: LiveData<Event<Unit>> = _retrievingContactsError

    private val _updatingContactError = MutableLiveData<Event<Unit>>()
    val updatingContactError: LiveData<Event<Unit>> = _updatingContactError

    private val _creatingContactError = MutableLiveData<Event<Unit>>()
    val creatingContactError: LiveData<Event<Unit>> = _creatingContactError

    private val _removingContactError = MutableLiveData<Event<Unit>>()
    val removingContactError: LiveData<Event<Unit>> = _removingContactError

    private val _startingToSendCatalog = MutableLiveData<Event<String>>()
    val startingToSendCatalog: LiveData<Event<String>> = _startingToSendCatalog

    private val _catalogSentSuccessfully = MutableLiveData<Event<String>>()
    val catalogSentSuccessfully: LiveData<Event<String>> = _catalogSentSuccessfully

    private val _catalogSentError = MutableLiveData<Event<String>>()
    val catalogSentError: LiveData<Event<String>> = _catalogSentError

    private val _secretCopied = MutableLiveData<Event<Unit>>()
    val secretCopied: LiveData<Event<Unit>> = _secretCopied

    private val _changingSecretFailed = MutableLiveData<Event<Unit>>()
    val changingSecretFailed: LiveData<Event<Unit>> = _changingSecretFailed

    private val _accountDeletedSuccessfully = MutableLiveData<Event<Unit>>()
    val accountDeletedSuccessfully: LiveData<Event<Unit>> = _accountDeletedSuccessfully

    private val _accountDeletionFailed = MutableLiveData<Event<Unit>>()
    val accountDeletionFailed: LiveData<Event<Unit>> = _accountDeletionFailed

    val user = ObservableField<User?>()

    private fun subscribeToContactUpdates() {
        contactsUpdateDisposable = remoteRepository.getContacts().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                contacts = it
            }, {
                _retrievingContactsError.value = Event(Unit)
                it.printStackTrace()
            })
        disposables.add(contactsUpdateDisposable)
    }

    private fun unsubscribeFromContactsUpdates() {
        contactsUpdateDisposable.dispose()
    }

    private fun onNewDataReceive() {
        _contactsUpdated.value = Event(Unit)
    }

    // todo проверить поведение при множественных кликах до окончания логина. Возможно нужно будет обработать это поведение и поставить фильтр (флаг)
    fun onGoogleButtonClick() {
        if (user.get() == null) signIn() else signOut()
    }

    private fun signIn() {
        Log.d(TAG, "signIn: ")
        _signInClicked.value = Event(Unit)
    }

    fun signOut() {
        disposables.add(remoteRepository.removeTokenOnLogout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                FirebaseAuth.getInstance().signOut()
                doSignOutCompanionOperations()
            }
            .subscribe({
                //do nothing
            }, {
                Log.e(TAG, "signOut: failed ")
                // пропускаем исключения, т.к. нам по факту не важно, успешен был логаут или нет)
            }))
    }

    // Дополнительные методы для очищения ресурсов и обновления интерфейса после логаута
    private fun doSignOutCompanionOperations() {
        _signOutClicked.value = Event(Unit)
        user.set(null)
        unsubscribeFromContactsUpdates()
    }

    fun onAddContactClick() {
        _addContactClicked.value = Event(Unit)
    }

    fun signInToFirebase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        disposables.add(remoteRepository.signInToFirebase(credential)
            .andThen(remoteRepository.addUserUpdateTokenGetSecret())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _signInComplete.value = Event(Unit)
                val firebaseUser = FirebaseAuth.getInstance().currentUser!!
                user.set(User(firebaseUser.displayName ?: "",
                    firebaseUser.email ?: "",
                    firebaseUser.photoUrl, it, credential))
                subscribeToContactUpdates()
            }, {
                signOut()
                it.printStackTrace()
                _firebaseSignInError.value = Event(Unit)
            }))
    }

    override fun onDeleteYesClick(contact: Contact) {
        disposables.add(remoteRepository.removeContact(contact).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({

            }, {
                _removingContactError.value = Event(Unit)
                it.printStackTrace()
            }))
    }

    override fun onEditYesClick(action: ContactDialogAction, contact: Contact) {
        val name = contact.name
        val shortId = contact.secret
        val newContactData = mapOf(
            RemoteDbSchema.FRIEND_NAME to name,
            RemoteDbSchema.FRIEND_SHORT_ID to shortId
        )
        disposables.add(
            when (action) {
                ContactDialogAction.CREATE -> {

                    remoteRepository.addContact(newContactData).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({
                            // do nothing
                        }, {
                            _creatingContactError.value = Event(Unit)
                            it.printStackTrace()
                        })
                }
                ContactDialogAction.RENAME -> {
                    remoteRepository.updateContact(newContactData).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({
                            // do nothing
                        }, {
                            _updatingContactError.value = Event(Unit)
                            it.printStackTrace()
                        })
                }
                ContactDialogAction.RENAME_NOT_VALID -> {
                    throw UnsupportedOperationException("Can't create or update invalid contacts")
                }
            })
    }

    fun sendCatalog(catalogContent: Map<String, Any>) {
        val catalogName = catalogContent[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
        _startingToSendCatalog.value = Event(catalogName)
        isSending.set(true)
        sendingCatalogName.set(catalogName)
        disposables.add(remoteRepository.sendCatalog(catalogContent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                isSending.set(false)
                _catalogSentSuccessfully.value = Event(catalogName)
            }, {
                isSending.set(false)
                _catalogSentError.value = Event(catalogName)
                it.printStackTrace()
            }))
    }

    fun onDeleteAccountYesClick() {
        Log.d(TAG, "onDeleteAccountYesClick: ")
        disposables.add(remoteRepository.reauthentificateInFirebase(user.get()!!.credential)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(remoteRepository.deleteAccount())
            .subscribe({
                _accountDeletedSuccessfully.value = Event(Unit)
                doSignOutCompanionOperations()
            },
                {
                    it.printStackTrace()
                    _accountDeletionFailed.value = Event(Unit)
                }))
    }

    override fun nameHasDuplicates(name: String): Boolean =
        contacts.any { it.name == name }

    override fun onContactSwipePerform(contact: Contact) {
        _contactSwipePerformed.value = Event(contact)
    }

    override fun onContactEditClick(contact: Contact) {
        _contactEditClicked.value = Event(contact)
    }

    override fun getContacts(): List<Contact> = contacts

    override fun onCleared() {
        disposables.clear()
    }

    fun copySecretToClipboard(context: Context) {
        copyTextToClipboard(context, user.get()?.secret ?: "", "secret_code_value")
        _secretCopied.value = Event(Unit)
        Log.d(TAG, "copySecretToClipboard: ")
    }

    fun reloadSecretCode() {
        Log.d(TAG, "reloadSecretCode: ")
        disposables.add(remoteRepository.changeSecret()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newSecret ->
                user.get()!!.secret = newSecret
                user.notifyPropertyChanged(BR.secret)
            }, {
                it.printStackTrace()
                _changingSecretFailed.value = Event(Unit)
            }))

    }
}

class NetworkVmFactory(
    private val remoteRepository: RemoteRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkViewModel::class.java)) {
            return NetworkViewModel(remoteRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}