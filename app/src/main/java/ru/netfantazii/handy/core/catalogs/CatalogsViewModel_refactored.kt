package ru.netfantazii.handy.core.catalogs

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.core.*
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.extensions.*
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import ru.netfantazii.handy.data.database.GeofenceEntity
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.repositories.LocalRepository
import java.util.*
import javax.inject.Inject

class CatalogsViewModel_refactored @Inject constructor(localRepository: LocalRepository) :
    ViewModel(),
    CatalogClickHandler, CatalogStorage, OverlayActions, DialogClickHandler {
    private val TAG = "CatalogsViewModel"

    override var overlayBuffer: BufferObject
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override val catalogAndNetInfoReceived: LiveData<Event<Pair<Catalog, CatalogNetInfoEntity>>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun onCatalogClick(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogSwipeStart(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogSwipePerform(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogSwipeFinish(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogSwipeCancel(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogEditClick(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogDragSucceed(fromPosition: Int, toPosition: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogNotificationClick(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogShareClick(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCatalogEnvelopeClick(catalog: Catalog) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCatalogList(): List<Catalog> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOverlayBackgroundClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOverlayEnterClick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}