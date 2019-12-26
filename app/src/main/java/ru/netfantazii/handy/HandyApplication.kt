package ru.netfantazii.handy

import android.app.Application
import ru.netfantazii.handy.core.ServiceLocator
import ru.netfantazii.handy.db.Catalog

class HandyApplication : Application() {
    val localRepository: LocalRepository
    get() = ServiceLocator.provideLocalRepository(this)

    override fun onCreate() {
        super.onCreate()
    }
}