package ru.netfantazii.handy

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * A custom [AndroidJUnitRunner] used to replace the application used in tests with a
 * [MyTestApplication].
 */
class HandyTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HandyTestApplication::class.java.name, context)
    }
}