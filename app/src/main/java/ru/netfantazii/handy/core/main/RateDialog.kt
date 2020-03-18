package ru.netfantazii.handy.core.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.Constants
import ru.netfantazii.handy.extensions.navigateToPlayMarket
import java.util.*

class RateDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setIcon(R.drawable.ic_solid_star_dialog)
            .setTitle(R.string.rate_app_title)
            .setMessage(R.string.rate_app_message)
            .setPositiveButton(R.string.rate_yes_button) { _, _ -> rateYes() }
            .setNegativeButton(R.string.rate_later_button) { _, _ -> rateLater() }
            .setNeutralButton(R.string.rate_never_show_button) { _, _ -> rateNever() }
            .create()
    }

    private fun rateYes() {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sp.edit()
            .putBoolean(Constants.NEVER_SHOW_AGAIN_KEY, true)
            .apply()
        navigateToPlayMarket(requireContext())
    }

    private fun rateLater() {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sp.edit()
            .putLong(Constants.FIRST_LAUNCH_DATE_KEY, Date().time)
            .putInt(Constants.LAUNCH_COUNT_KEY, 0)
            .apply()
    }

    private fun rateNever() {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sp.edit()
            .putBoolean(Constants.NEVER_SHOW_AGAIN_KEY, true)
            .apply()
    }
}