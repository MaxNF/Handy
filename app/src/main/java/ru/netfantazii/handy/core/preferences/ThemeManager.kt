package ru.netfantazii.handy.core.preferences

import android.content.Context
import android.util.TypedValue
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.preferences.ThemeColor.*
import java.lang.UnsupportedOperationException

enum class ThemeColor {
    COLOR_PRIMARY, COLOR_PRIMARY_DARK, COLOR_ACCENT, CATALOG_NORMAL_COLOR, CATALOG_SWIPE_COLOR,
    BOUGHT_NORMAL_COLOR, BOUGHT_SWIPE_COLOR, NOT_BOUGHT_NORMAL_COLOR, NOT_BOUGHT_SWIPE_COLOR,
    TOOLBAR_ELEMENT_COLOR, ICONS_COLOR, OVERLAY_BACKGROUND_COLOR, SPEED_DIAL_LABEL_BACKGROUND, SPEED_DIAL_LABEL_COLOR,
    SWIPE_BACKGROUND_COLOR, FAB_ICON_TINT, SNACK_BAR_ACTION_COLOR
}

fun getThemeColor(context: Context, themeColor: ThemeColor?): Int {
    val theme = context.theme
    val typedValue = TypedValue()
    when (themeColor) {
        COLOR_ACCENT -> theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        COLOR_PRIMARY -> theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        COLOR_PRIMARY_DARK -> theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        CATALOG_NORMAL_COLOR -> theme.resolveAttribute(R.attr.catalogNormalColor,
            typedValue,
            true)
        CATALOG_SWIPE_COLOR -> theme.resolveAttribute(R.attr.catalogSwipeColor,
            typedValue,
            true)
        TOOLBAR_ELEMENT_COLOR -> theme.resolveAttribute(R.attr.toolbarElementColor,
            typedValue,
            true)
        ICONS_COLOR -> theme.resolveAttribute(R.attr.listIconsColor, typedValue, true)
        BOUGHT_SWIPE_COLOR -> theme.resolveAttribute(R.attr.boughtSwipeColor, typedValue, true)
        BOUGHT_NORMAL_COLOR -> theme.resolveAttribute(R.attr.boughtNormalColor,
            typedValue,
            true)
        NOT_BOUGHT_SWIPE_COLOR -> theme.resolveAttribute(R.attr.notBoughtSwipeColor,
            typedValue,
            true)
        NOT_BOUGHT_NORMAL_COLOR -> theme.resolveAttribute(R.attr.notBoughtNormalColor,
            typedValue,
            true)
        SPEED_DIAL_LABEL_COLOR -> theme.resolveAttribute(R.attr.speedDialActionLabelColor,
            typedValue,
            true)
        SPEED_DIAL_LABEL_BACKGROUND -> theme.resolveAttribute(R.attr.speedDialActionLabelBackground,
            typedValue,
            true)
        SWIPE_BACKGROUND_COLOR -> theme.resolveAttribute(R.attr.swipeBackgroundColor,
            typedValue,
            true)
        OVERLAY_BACKGROUND_COLOR -> theme.resolveAttribute(R.attr.overlayBackgroundColor,
            typedValue,
            true)
        FAB_ICON_TINT -> theme.resolveAttribute(R.attr.fabIconTintColor, typedValue, true)
        SNACK_BAR_ACTION_COLOR -> theme.resolveAttribute(R.attr.snackbarActionColor,
            typedValue,
            true)
    }
    return typedValue.data
}

fun getCurrentThemeValue(context: Context): String {
    val sp =
        PreferenceManager.getDefaultSharedPreferences(context)
    return sp.getString(
        context.getString(R.string.theme_pref_key),
        context.getString(R.string.theme_violet_value)
    ) ?: throw UnsupportedOperationException("No such a theme")
}

fun setTheme(context: Context, themeValue: String) {
    context.setTheme(
        when (themeValue) {
            context.getString(R.string.theme_violet_value) -> R.style.Purple
            context.getString(R.string.theme_lilac_value) -> R.style.Lilac
            context.getString(R.string.theme_ocean_value) -> R.style.Ocean
            context.getString(R.string.theme_wood_value) -> R.style.Wood
            context.getString(R.string.theme_green_value) -> R.style.Green
            context.getString(R.string.theme_sunny_value) -> R.style.Sunny
            context.getString(R.string.theme_crimson_value) -> R.style.Crimson
            context.getString(R.string.theme_orange_value) -> R.style.Orange
            context.getString(R.string.theme_indigo_value) -> R.style.Indigo
            context.getString(R.string.theme_gray_value) -> R.style.Gray
            else -> throw IllegalArgumentException("Unknown theme")
        }
    )
}