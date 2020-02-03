package ru.netfantazii.handy.model

enum class BuyStatus(val number: Int) {
    BOUGHT(1), NOT_BOUGHT(2)
}

enum class ExpandStatus(val number: Int) {
    EXPANDED(1), COLLAPSED(2)
}

enum class GroupType(val number: Int) {
    ALWAYS_ON_TOP(1), STANDARD(2)
}

enum class HintType {
    EXPAND, COLLAPSE, EMPTY, EMPTY_UNSORTED
}

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST
}

