package ru.netfantazii.handy.core.main

import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
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
import ru.netfantazii.handy.data.Contact
import ru.netfantazii.handy.data.ContactDialogAction
import ru.netfantazii.handy.data.PbOperations
import ru.netfantazii.handy.data.User
import ru.netfantazii.handy.data.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.RemoteRepository
import java.lang.UnsupportedOperationException

class NetworkViewModel(private val remoteRepository: RemoteRepository) : ViewModel(),
    ContactsStorage, DialogClickHandler, ContactsClickHandler {
    private val TAG = "NetworkViewModel"

    private val disposables = CompositeDisposable()
    private lateinit var contactsUpdateDisposable: Disposable

    lateinit var dialogBuffer: Pair<ContactDialogAction, Contact?>

    private var contacts: List<Contact> = listOf()
        set(value) {
            Log.d(TAG, ": ")
            field = value
            onNewDataReceive()
        }

    val sendingCatalogName = ObservableField<String>()

    private val _contactSwipePerformed = MutableLiveData<Event<Unit>>()
    val contactSwipePerformed: LiveData<Event<Unit>> = _contactSwipePerformed

    private val _contactEditClicked = MutableLiveData<Event<Unit>>()
    val contactEditClicked: LiveData<Event<Unit>> = _contactEditClicked

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

    private val _startingToSendCatalog = MutableLiveData<Event<String>>()
    val startingToSendCatalog: LiveData<Event<String>> = _startingToSendCatalog

    private val _catalogSentSuccessfully = MutableLiveData<Event<String>>()
    val catalogSentSuccessfully: LiveData<Event<String>> = _catalogSentSuccessfully

    private val _secretCopied = MutableLiveData<Event<Unit>>()
    val secretCopied: LiveData<Event<Unit>> = _secretCopied

    private val _changingSecretFailed = MutableLiveData<Event<Unit>>()
    val changingSecretFailed: LiveData<Event<Unit>> = _changingSecretFailed

    private val _accountDeletedSuccessfully = MutableLiveData<Event<Unit>>()
    val accountDeletedSuccessfully: LiveData<Event<Unit>> = _accountDeletedSuccessfully

    private val _accountDeletionFailed = MutableLiveData<Event<Unit>>()
    val accountDeletionFailed: LiveData<Event<Unit>> = _accountDeletionFailed

    private val _showProgressBar = MutableLiveData<Event<PbOperations>>()
    val showProgressBar: LiveData<Event<PbOperations>> = _showProgressBar

    private val _hideProgressBar = MutableLiveData<Event<Unit>>()
    val hideProgressBar: LiveData<Event<Unit>> = _hideProgressBar

    private val _shareSecretCodeClicked = MutableLiveData<Event<String>>()
    val shareSecretCodeClicked: LiveData<Event<String>> = _shareSecretCodeClicked

    val user = ObservableField<User?>()

    val inputFilter = InputFilter()

    private fun subscribeToContactUpdates() {
        contactsUpdateDisposable = remoteRepository.getContacts().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                contacts = it
            }
        disposables.add(contactsUpdateDisposable)
    }

    private fun unsubscribeFromContactsUpdates() {
        contactsUpdateDisposable.dispose()
    }

    private fun onNewDataReceive() {
        _contactsUpdated.value = Event(Unit)
    }

    fun onGoogleButtonClick() {
        if (user.get() == null) signIn() else signOut()
    }

    private fun signIn() {
        showPb(PbOperations.SIGNING_IN)
        _signInClicked.value = Event(Unit)
    }

    fun showPb(operation: PbOperations) {
        _showProgressBar.value = Event(operation)
    }

    fun hidePb() {
        _hideProgressBar.value = Event(Unit)
    }

    fun signOut() {
        showPb(PbOperations.SIGNING_OUT)
        disposables.add(remoteRepository.removeTokenOnLogout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate {
                FirebaseAuth.getInstance().signOut()
                doSignOutCompanionOperations()
                hidePb()
            }
            .subscribe())
    }

    // Дополнительные методы для очищения ресурсов и обновления интерфейса после логаута
    private fun doSignOutCompanionOperations() {
        _signOutClicked.value = Event(Unit)
        user.set(null)
        unsubscribeFromContactsUpdates()
    }

    fun onAddContactClick() {
        dialogBuffer = Pair(ContactDialogAction.CREATE, null)
        _addContactClicked.value = Event(Unit)
    }

    // вызывается автоматически следом за успешной операцией входа в гугл аккаунт
    fun signInToFirebase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        disposables.add(remoteRepository.signInToFirebase(credential)
            .andThen(remoteRepository.addUserUpdateTokenGetSecret())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (first, second) ->
                //first - short_id (секретный код), second - yandex api key
                _signInComplete.value = Event(Unit)
                val firebaseUser = FirebaseAuth.getInstance().currentUser!!
                user.set(User(firebaseUser.displayName ?: "",
                    firebaseUser.email ?: "",
                    firebaseUser.photoUrl, first, credential, second))
                subscribeToContactUpdates()
                hidePb()
            }, {
                _firebaseSignInError.value = Event(Unit)
                hidePb()
            }))
    }

    override fun onDeleteYesClick(contact: Contact) {
        val shortId = contact.secret
        val contactDataToDelete = mapOf(
            RemoteDbSchema.FRIEND_SHORT_ID to shortId
        )
        showPb(PbOperations.UPDATING_CLOUD_DATABASE)
        disposables.add(remoteRepository.removeContact(contactDataToDelete).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                hidePb()
            })
    }

    override fun onEditYesClick(action: ContactDialogAction, contact: Contact) {
        val name = contact.name
        val shortId = contact.secret
        val newContactData = mapOf(
            RemoteDbSchema.FRIEND_NAME to name,
            RemoteDbSchema.FRIEND_SHORT_ID to shortId
        )
        showPb(PbOperations.UPDATING_CLOUD_DATABASE)
        disposables.add(
            when (action) {
                ContactDialogAction.CREATE -> {
                    remoteRepository.addContact(newContactData).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe {
                            hidePb()
                        }
                }
                ContactDialogAction.RENAME -> {
                    remoteRepository.updateContact(newContactData).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe {
                            hidePb()
                        }
                }
                ContactDialogAction.RENAME_NOT_VALID -> {
                    throw UnsupportedOperationException("Can't create or update invalid contacts")
                }
                ContactDialogAction.DELETE -> {
                    throw UnsupportedOperationException("Deletion should be performed in the corresponded method")
                }
            })
    }

    fun sendCatalog(catalogContent: Map<String, Any>) {
        val catalogName = catalogContent[RemoteDbSchema.MESSAGE_CATALOG_NAME] as String
        _startingToSendCatalog.value = Event(catalogName)
        sendingCatalogName.set(catalogName)

        showPb(PbOperations.SENDING_CATALOG)
        disposables.add(remoteRepository.sendCatalog(catalogContent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _catalogSentSuccessfully.value = Event(catalogName)
                hidePb()
            })
    }

    fun onDeleteAccountYesClick() {
        Log.d(TAG, "onDeleteAccountYesClick: ")
        showPb(PbOperations.DELETING_ACCOUNT)
        disposables.add(remoteRepository.reauthentificateInFirebase(user.get()!!.credential)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(remoteRepository.deleteAccount())
            .subscribe {
                _accountDeletedSuccessfully.value = Event(Unit)
                doSignOutCompanionOperations()
                hidePb()
            })
    }

    override fun nameHasDuplicates(name: String): Boolean =
        contacts.any { it.name == name }

    override fun onContactDeleteClick(contact: Contact) {
        dialogBuffer = Pair(ContactDialogAction.DELETE, contact)
        _contactSwipePerformed.value = Event(Unit)
    }

    override fun onContactEditClick(contact: Contact) {
        Log.d(TAG, "onContactEditClick: ")
        val action =
            if (contact.isValid) ContactDialogAction.RENAME else ContactDialogAction.RENAME_NOT_VALID
        dialogBuffer = Pair(action, contact)
        _contactEditClicked.value = Event(Unit)
    }

    override fun getContacts(): List<Contact> = contacts

    override fun getValidContacts(): List<Contact> = contacts.filter { it.isValid }

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
        showPb(PbOperations.UPDATING_CLOUD_DATABASE)
        disposables.add(remoteRepository.changeSecret()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { newSecret ->
                user.get()!!.secret = newSecret
                user.notifyPropertyChanged(BR.secret)
                inputFilter.lastSecretChangeTime = System.currentTimeMillis()
                hidePb()
            })
    }

    fun shareSecretCode() {
        _shareSecretCodeClicked.value = Event(user.get()?.secret ?: "n/a")
    }
}

class NetworkVmFactory(
    private val remoteRepository: RemoteRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworkViewModel::class.java)) {
            return NetworkViewModel(
                remoteRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}