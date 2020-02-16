package ru.netfantazii.handy.core.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.model.Group
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository

class ShareViewModel(
    private val catalogId: Long,
    val catalogName: String,
    val totalProducts: String,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _sendClicked = MutableLiveData<Event<Map<String, Any>>>()
    val sendClicked: LiveData<Event<Map<String, Any>>> = _sendClicked

    private val disposables = CompositeDisposable()
    private lateinit var parsedGroups: List<Map<String, Any>>
    var comment: String = ""

    init {
        getCatalogContent()
    }

    private fun getCatalogContent() {
        disposables.add(localRepository.getGroups(catalogId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                parsedGroups = parseGroups(it)
            })
    }

    private fun parseGroups(groupList: List<Group>): List<Map<String, Any>> {
        return groupList.map { group ->
            val products = group.productList.map { it.name }
            mapOf(
                RemoteDbSchema.MESSAGE_GROUP_NAME to group.name,
                RemoteDbSchema.MESSAGE_GROUP_PRODUCTS to products)
        }
    }

    fun onSendClick(contact: Contact) {
        val content = mapOf(
            RemoteDbSchema.MESSAGE_TO_SECRET to contact.secret,
            RemoteDbSchema.MESSAGE_CATALOG_NAME to catalogName,
            RemoteDbSchema.MESSAGE_CATALOG_COMMENT to comment,
            RemoteDbSchema.MESSAGE_CATALOG_CONTENT to parsedGroups
        )
        _sendClicked.value = Event(content)
    }
}

class ShareVmFactory(
    private val catalogId: Long,
    private val catalogName: String,
    private val totalProducts: String,
    private val localRepository: LocalRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            return ShareViewModel(catalogId,
                catalogName,
                totalProducts,
                localRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}