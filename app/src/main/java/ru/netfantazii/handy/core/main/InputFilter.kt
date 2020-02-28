package ru.netfantazii.handy.core.main

class InputFilter {
    private val millisInMinute = 60 * 1000

    var netActionAllowed = true

    val changeSecretIsNotInTimeout: Boolean
        get() {
            val elapsedTime = System.currentTimeMillis() - lastSecretChangeTime
            return elapsedTime > millisInMinute
        }

    var lastSecretChangeTime: Long = 0L
}