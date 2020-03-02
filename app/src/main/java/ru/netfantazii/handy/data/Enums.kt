package ru.netfantazii.handy.data

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

enum class ContactDialogAction {
    CREATE, RENAME, RENAME_NOT_VALID, DELETE
}

enum class PbOperations {
    SENDING_CATALOG, SIGNING_OUT, SIGNING_IN, UPDATING_CLOUD_DATABASE, DELETING_ACCOUNT
}

enum class BillingPriceTypes {
    ONE_MONTH_PRICE, ONE_YEAR_PRICE, FOREVER_PRICE
}
