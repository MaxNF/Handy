package ru.netfantazii.handy

import java.util.*

class InputFilter {
    var netActionAllowed = true

    var changeSecretAllowed = true
    var previousSecretChangeTime: Calendar? = null
}