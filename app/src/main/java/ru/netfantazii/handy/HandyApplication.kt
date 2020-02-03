package ru.netfantazii.handy

import android.app.Application
import ru.netfantazii.handy.core.ServiceLocator
import ru.netfantazii.handy.repositories.LocalRepository

class HandyApplication : Application() {
    val localRepository: LocalRepository
        get() = ServiceLocator.provideLocalRepository(this)
}