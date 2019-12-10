package ru.netfantazii.handy.db

import androidx.room.TypeConverter

enum class BuyStatus(val number: Int) {
    BOUGHT(1), NOT_BOUGHT(2)
}

enum class ExpandStatus(val number: Int) {
    EXPANDED(1), COLLAPSED(2)
}

enum class GroupType(val number: Int) {
    ALWAYS_ON_TOP(1), STANDARD(2)
}

