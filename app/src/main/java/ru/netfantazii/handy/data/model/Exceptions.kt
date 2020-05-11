package ru.netfantazii.handy.data.model

import com.android.billingclient.api.BillingClient.*
import java.lang.RuntimeException

class GeofenceLimitException : RuntimeException()

class BillingException(reason: String) : RuntimeException(reason) {
    constructor(code: Int) : this(codeToString(
        code))

    companion object BillingExceptionCodes {
        const val SERVICE_TIMEOUT = "service_timeout"
        const val FEATURE_NOT_SUPPORTED = "feature_not_supported"
        const val SERVICE_DISCONNECTED = "service_disconnected"
        const val USER_CANCELED = "user_canceled"
        const val SERVICE_UNAVAILABLE = "service_unavailable"
        const val BILLING_UNAVAILABLE = "billing_unavailable"
        const val ITEM_UNAVAILABLE = "item_unavailable"
        const val DEVELOPER_ERROR = "developer_error"
        const val ERROR = "error"
        const val ITEM_ALREADY_OWNED = "item_already_owned"
        const val ITEM_NOT_OWNED = "item_not_owned"

        const val UNKNOWN_ERROR_CODE = "unknown_ERROR_CODE"

        fun codeToString(code: Int) = when (code) {
            BillingResponseCode.BILLING_UNAVAILABLE -> BILLING_UNAVAILABLE
            BillingResponseCode.DEVELOPER_ERROR -> DEVELOPER_ERROR
            BillingResponseCode.ERROR -> ERROR
            BillingResponseCode.FEATURE_NOT_SUPPORTED -> FEATURE_NOT_SUPPORTED
            BillingResponseCode.SERVICE_TIMEOUT -> SERVICE_TIMEOUT
            BillingResponseCode.SERVICE_DISCONNECTED -> SERVICE_DISCONNECTED
            BillingResponseCode.USER_CANCELED -> USER_CANCELED
            BillingResponseCode.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE
            BillingResponseCode.ITEM_UNAVAILABLE -> ITEM_UNAVAILABLE
            BillingResponseCode.ITEM_ALREADY_OWNED -> ITEM_ALREADY_OWNED
            BillingResponseCode.ITEM_NOT_OWNED -> ITEM_NOT_OWNED
            else -> UNKNOWN_ERROR_CODE
        }
    }
}