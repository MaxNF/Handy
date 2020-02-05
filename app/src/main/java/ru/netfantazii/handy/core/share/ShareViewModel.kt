package ru.netfantazii.handy.core.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.model.Group
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository

class ShareViewModel(
    private val catalogId: Long,
    val catalogName: String,
    val totalProducts: String,
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

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

    fun sendCatalog(secret: String, comment: String) {
        disposables.add(remoteRepository.sendCatalog(secret, catalogName, comment, parsedGroups)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // todo success
            }, {
                // todo error
            }))
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
            return ShareViewModel(catalogId, catalogName, totalProducts, localRepository, remoteRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}