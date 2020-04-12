package ru.netfantazii.handy.core.main

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.plugins.RxJavaPlugins
import ru.netfantazii.handy.BuildConfig
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.BillingException
import ru.netfantazii.handy.data.database.ErrorCodes
import ru.netfantazii.handy.extensions.showLongToast
import java.io.IOException
import java.net.UnknownHostException


fun initGlobalRxErrorHandler(context: Context, networkViewModel: NetworkViewModel) {
    RxJavaPlugins.setErrorHandler { e ->
        if (BuildConfig.DEBUG) {
            throw e
        }
        if (e.cause?.cause is UnknownHostException) {
            showLongToast(context, context.getString(R.string.no_internet_error_message))
        } else
            when (e.cause) {
                is BillingException -> {
                    // do nothing. Все нужные исключения обрабатываются во View Model.
                }
                else -> when (e.cause?.message) {
                    ErrorCodes.DEADLINE_EXCEEDED -> {
                        showLongToast(context, context.getString(R.string.network_timeout_message))
                    }
                    ErrorCodes.DATA_PAYLOAD_IS_NULL -> {
                        showLongToast(context, "Data payload error")
                    }
                    ErrorCodes.FOUND_USER_DUPLICATE -> {
                        showLongToast(context, "Found user duplicate")
                    }
                    ErrorCodes.INSTANCE_ID_TOKEN_NOT_FOUND -> {
                        showLongToast(context, "Instance Id token not found")
                    }
                    ErrorCodes.MESSAGE_IS_EMPTY -> {
                        showLongToast(context, "Message is empty")
                    }
                    ErrorCodes.USER_IS_NOT_FOUND -> {
                        showLongToast(context, context.getString(R.string.user_not_found_error))
                    }
                    ErrorCodes.NO_MESSAGES_SENT -> {
                        showLongToast(context, context.getString(R.string.no_messages_sent_error))
                    }
                    ErrorCodes.MESSAGE_FAILED_DUE_INCORRECT_SECRET -> {
                        showLongToast(context,
                            context.getString(R.string.message_failed_incorrect_secret_error))
                    }
                    ErrorCodes.USER_IS_NOT_LOGGED_IN -> {
                        showLongToast(context, "Authentication error. Not logged in")
                    }
                    else -> {
                        showLongToast(context, context.getString(R.string.unknown_error_occurred))
                        e.cause?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                        e.printStackTrace()
                    }
                }
            }
        networkViewModel.hidePb()
    }
}