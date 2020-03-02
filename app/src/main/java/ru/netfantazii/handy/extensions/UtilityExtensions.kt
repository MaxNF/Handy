package ru.netfantazii.handy.extensions

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.BUNDLE_NOTIFICATION_ID_KEY
import ru.netfantazii.handy.core.notifications.CANCEL_NOTIFICATION_ACTION
import ru.netfantazii.handy.core.notifications.NotificationBroadcastReceiver
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.SortOrder


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

fun showShortToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showLongToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun reloadActivity(activity: Activity) {
    val intent = activity.intent
    activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun copyTextToClipboard(context: Context, text: String, description: String) {
    val clipboard: ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(description, text)
    clipboard?.setPrimaryClip(clip)
}

fun getNewCatalogPosition(catalogList: List<Catalog>): Int {
    return when (currentSortOrder) {
        SortOrder.NEWEST_FIRST -> 0
        SortOrder.OLDEST_FIRST -> catalogList.size
    }
}

fun getCancelPendingIntentForNotifications(context: Context, notificationId: Int): PendingIntent {
    val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
        action = CANCEL_NOTIFICATION_ACTION
        putExtra(BUNDLE_NOTIFICATION_ID_KEY, notificationId)
    }
    return PendingIntent.getBroadcast(context, notificationId, cancelIntent, 0)
}

fun getNotificationSoundUri() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

fun getAlarmSoundUri() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)