package ru.netfantazii.handy.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import ru.netfantazii.handy.R
import ru.netfantazii.handy.db.SortOrder

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

fun dpToPx(dp: Int): Float {
    return dp * Resources.getSystem().displayMetrics.density
}

fun pxToDp(px: Int): Float {
    return px / Resources.getSystem().displayMetrics.density
}

fun getSortOrder(context: Context): SortOrder {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val sortingKey = context.getString(R.string.sorting_pref_key)
    val sortingNewestFirstValue = context.getString(R.string.sorting_newest_first_value)
    val sortingOldestFirstValue = context.getString(R.string.sorting_oldest_first_value)

    return when (sharedPref.getString(sortingKey, sortingNewestFirstValue)) {
        sortingOldestFirstValue -> SortOrder.OLDEST_FIRST
        else -> SortOrder.NEWEST_FIRST
    }
}

fun getRequiredMapPermissions(): Array<String> {
    val permissions = mutableListOf(Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    return permissions.toTypedArray()
}