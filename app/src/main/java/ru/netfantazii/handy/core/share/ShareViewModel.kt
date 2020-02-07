package ru.netfantazii.handy.core.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.model.Group
import ru.netfantazii.handy.model.database.RemoteDbSchema
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository

class ShareViewModel(
    private val catalogId: Long,
    val catalogName: String,
    val totalProducts: String,
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _sendClicked = MutableLiveData<Event<Map<String, Any>>>()
    val sendClicked: LiveData<Event<Map<String, Any>>> = _sendClicked

    private val disposables = CompositeDisposable()
    private lateinit var parsedGroups: Map<String, Map<String, Any>>
    var secret: String = ""
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

    private fun parseGroups(groupList: List<Group>): Map<String, Map<String, Any>> {
        return mapOf()
    }

    fun onSendClick() {
        val content = mapOf(
            RemoteDbSchema.MESSAGE_TO_SECRET to secret,
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
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            return ShareViewModel(catalogId,
                catalogName,
                totalProducts,
                localRepository,
                remoteRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}