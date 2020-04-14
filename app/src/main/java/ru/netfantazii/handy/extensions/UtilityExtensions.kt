package ru.netfantazii.handy.extensions

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.yandex.mapkit.geometry.Point
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

fun computeOffset(point: Point, distance: Double, heading: Double): Point {
    var distance = distance
    var heading = heading
    distance /= 6371009.0
    heading = Math.toRadians(heading)
    val fromLat = Math.toRadians(point.latitude)
    val fromLng = Math.toRadians(point.longitude)
    val cosDistance = Math.cos(distance)
    val sinDistance = Math.sin(distance)
    val sinFromLat = Math.sin(fromLat)
    val cosFromLat = Math.cos(fromLat)
    val sinLat =
        cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading)
    val dLng =
        Math.atan2(sinDistance * cosFromLat * Math.sin(heading),
            cosDistance - sinFromLat * sinLat)
    return Point(Math.toDegrees(Math.asin(sinLat)),
        Math.toDegrees(fromLng + dLng))
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

fun defaultVibrationPattern(): LongArray = longArrayOf(0L, 500, 200, 500, 200, 500)

fun createFunnyVibrationPattern(): LongArray = longArrayOf(0L,
    225, 225, 225, 225,
    112, 112, 112, 112, 225, 225,
    112, 112, 112, 112, 112, 112, 225,
    225, 112, 112, 225)

fun navigateToPlayMarket(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${context.packageName}")))
    } catch (e: ActivityNotFoundException) {
        showLongToast(context, context.getString(R.string.google_play_not_found))
    }
}


inline fun <reified T : ViewModel> FragmentActivity.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.injectViewModel(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory)[T::class.java]
}