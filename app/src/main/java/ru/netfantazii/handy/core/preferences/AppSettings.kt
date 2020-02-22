package ru.netfantazii.handy.core.preferences

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.customviews.MyPreferenceButton
import ru.netfantazii.handy.model.SortOrder
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.extensions.reloadActivity
import ru.netfantazii.handy.extensions.showShortToast
import ru.netfantazii.handy.model.User

const val FIRST_LAUNCH_KEY = "first_launch"
const val SHOULD_SILENT_SIGN_IN_KEY = "silent_sign_in"
lateinit var currentSortOrder: SortOrder

class AppSettings : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "AppSettings"
    private lateinit var sp: SharedPreferences
    lateinit var viewModel: NetworkViewModel
    lateinit var deleteAccPref: MyPreferenceButton
    private val userChangedCallback = object :
        Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val user = (sender as ObservableField<User?>).get()
            if (user != null) {
                deleteAccPref.setNewSecretToView(user.secret)
            }
            deleteAccPref.isVisible = user != null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).checkMenuItem(R.id.navigation_app_settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.registerOnSharedPreferenceChangeListener(this)

        viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        deleteAccPref = findPreference("delete_account_button")!!
        deleteAccPref!!.isVisible = viewModel.user.get() != null
        deleteAccPref.setDeleteAccountAction { showDeleteAccConfirmationDialog() }
        deleteAccPref.setCopySecretAction { viewModel.copySecretToClipboard(requireContext()) }
        deleteAccPref.setGetNewSecretAction { showChangeSecretConfirmationDialog() }
        deleteAccPref.setNewSecretToView(viewModel.user.get()?.secret ?: "n/a")
        deleteAccPref.setShareSecretAction { viewModel.shareSecretCode() }

        val threadName = Thread.currentThread().name
        Log.d(TAG, "onCreatePreferences: $threadName")
    }

    override fun setDivider(divider: Drawable?) {
        val newDivider = context!!.getDrawable(android.R.drawable.divider_horizontal_textfield)
        if (newDivider != null) {
            super.setDivider(newDivider)
        } else {
            super.setDivider(divider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference: Preference? = findPreference(key)
        preference?.let {
            if (key == context!!.getString(R.string.theme_pref_key)) reloadActivity(activity!!)
            if (key == context!!.getString(R.string.sorting_pref_key)) currentSortOrder =
                getSortOrder(context!!)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.user.addOnPropertyChangedCallback(userChangedCallback)
    }

    override fun onStop() {
        super.onStop()
        viewModel.user.removeOnPropertyChangedCallback(userChangedCallback)
    }

    private fun showDeleteAccConfirmationDialog() {
        if (viewModel.inputFilter.netActionAllowed) {
            val dialog = DeleteAccConfirmDialog()
            dialog.show(childFragmentManager, "delete_acc_dialog")
        }
    }

    private fun showChangeSecretConfirmationDialog() {
        if (viewModel.inputFilter.netActionAllowed) {
            if (viewModel.inputFilter.changeSecretIsNotInTimeout) {
                val dialog = ChangeSecretConfirmDialog()
                dialog.show(childFragmentManager, "change_secret_dialog")
            } else {
                showShortToast(requireContext(), getString(R.string.change_secret_timeout_message))
            }
        }
    }
}

class DeleteAccConfirmDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        return AlertDialog.Builder(context, R.style.BaseDialogTheme)
            .setTitle(getString(R.string.delete_account_dialog_title))
            .setPositiveButton(getString(R.string.dialog_delete_account_action)) { _, _ ->
                viewModel.onDeleteAccountYesClick()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create()
    }
}

class ChangeSecretConfirmDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        return AlertDialog.Builder(context, R.style.BaseDialogTheme)
            .setTitle(getString(R.string.change_secret_dialog_title))
            .setMessage(R.string.change_secret_dialog_message)
            .setPositiveButton(getString(R.string.dialog_change_secret_action)) { _, _ ->
                viewModel.reloadSecretCode()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create()
    }
}