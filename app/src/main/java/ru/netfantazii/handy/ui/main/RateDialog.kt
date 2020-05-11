package ru.netfantazii.handy.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.model.Constants
import ru.netfantazii.handy.utils.extensions.navigateToPlayMarket

class RateDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext(), R.style.BaseDialogTheme)
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
        // do nothing (сброс счетчика происходит в HandyApplication. Поведение кнопки получается таким же как и клик по свобдному месту вне диалогового окна)
    }

    private fun rateNever() {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sp.edit()
            .putBoolean(Constants.NEVER_SHOW_AGAIN_KEY, true)
            .apply()
    }
}