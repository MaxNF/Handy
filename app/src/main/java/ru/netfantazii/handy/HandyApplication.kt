package ru.netfantazii.handy

import android.app.Application
import ru.netfantazii.handy.repositories.LocalRepository
import ru.netfantazii.handy.repositories.RemoteRepository

class HandyApplication : Application() {
    val localRepository: LocalRepository
        get() = ServiceLocator.provideLocalRepository(this)
    val remoteRepository: RemoteRepository
        get() = ServiceLocator.provideRemoteRepository(this)
}