package ru.netfantazii.handy.extensions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import java.lang.UnsupportedOperationException

fun doWithDelay(delayMillis: Long, action: () -> Unit) {
    Handler().postDelayed(action, delayMillis)
}

fun hideKeyboard(activity: Activity) {
    if (activity.window != null) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    } else {
        throw UnsupportedOperationException("Can't show keyboard. Activity's window is null.")
    }
}

fun showKeyboard(activity: Activity) {
    if (activity.window != null) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(activity.window.currentFocus, 0)
    } else {
        throw UnsupportedOperationException("Can't show keyboard. Activity's window is null.")
    }
}

