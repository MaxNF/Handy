package ru.netfantazii.handy.ui.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netfantazii.handy.data.model.Event
import ru.netfantazii.handy.data.usecases.group.SubscribeToGroupsChangesUseCase
import ru.netfantazii.handy.data.model.Contact
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.remotedb.RemoteDbSchema
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.di.TotalProducts
import javax.inject.Inject

class ShareViewModel @Inject constructor(
    @CatalogName val catalogName: String,
    @TotalProducts val totalProducts: String,
    private val subscribeToGroupsChangesUseCase: SubscribeToGroupsChangesUseCase
) : ViewModel() {

    private val _sendClicked = MutableLiveData<Event<Map<String, Any>>>()
    val sendClicked: LiveData<Event<Map<String, Any>>> = _sendClicked

    private val _sendClickedNoRecipient = MutableLiveData<Event<Unit>>()
    val sendClickedNoRecipient: LiveData<Event<Unit>> = _sendClickedNoRecipient

    private lateinit var parsedGroups: List<Map<String, Any>>
    var comment: String = ""

    init {
        getCatalogContent()
    }

    private fun getCatalogContent() {
        //todo ПЕРЕДЕЛАТЬ ПОД ЛАЙВ ДАТУ!
        subscribeToGroupsChangesUseCase.filteredAndNotFilteredGroups.observeForever { (notFilteredGroups, filteredGroups) ->
            parsedGroups = parseGroups(notFilteredGroups)
        }
    }

    private fun parseGroups(groupList: List<Group>): List<Map<String, Any>> {
        return groupList.map { group ->
            val products = group.productList.map { it.name }
            mapOf(
                RemoteDbSchema.MESSAGE_GROUP_NAME to group.name,
                RemoteDbSchema.MESSAGE_GROUP_PRODUCTS to products)
        }
    }

    fun onSendClick(contact: Contact?) {
        if (contact != null) {
            val content = mapOf(
                RemoteDbSchema.MESSAGE_TO_SECRET to contact.secret,
                RemoteDbSchema.MESSAGE_CATALOG_NAME to catalogName,
                RemoteDbSchema.MESSAGE_CATALOG_COMMENT to comment,
                RemoteDbSchema.MESSAGE_CATALOG_CONTENT to parsedGroups
            )
            _sendClicked.value = Event(content)
        } else {
            _sendClickedNoRecipient.value =
                Event(Unit)
        }
    }
}

//class ShareVmFactory(
//    private val catalogId: Long,
//    private val catalogName: String,
//    private val totalProducts: String,
//    private val localRepository: LocalRepository
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
//            return ShareViewModel(catalogId,
//                catalogName,
//                totalProducts,
//                localRepository) as T
//        }
//        throw IllegalArgumentException("Wrong ViewModel class")
//    }
//}