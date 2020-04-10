package ru.netfantazii.handy

import ru.netfantazii.handy.di.AppComponent
import ru.netfantazii.handy.di.DaggerTestAppComponent

class HandyTestApplication : HandyApplication() {
    override fun initializeComponent(): AppComponent {
        return DaggerTestAppComponent.factory()
            .create(applicationContext, applicationContext.packageName)
    }
}